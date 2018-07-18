package actions

import (
	"context"
	"log"
	"time"

	"github.com/gomods/athens/pkg/config/env"
	"github.com/gomods/athens/pkg/eventlog"
	"github.com/gomods/athens/pkg/module"
	"github.com/gomods/athens/pkg/storage"
	multierror "github.com/hashicorp/go-multierror"
)

// mergeDB merges diff into the module database.
//
// TODO: this is racey if multiple processes are running mergeDB (they will be!) in a few ways:
//
// 1. CDN updates that race to change the /list endpoint
// 2. races between CDN updates and module metadata updates. For example:
//		- Delete operation deletes from the CDN
//		- Add operation adds to the CDN and saves to the module metadata DB
//		- Delete operation adds tombstone to module metadata k/v store
//
// Both could be fixed by putting each 'for' loop into a (global) critical section
func mergeDB(ctx context.Context, originURL string, diff dbDiff, eLog eventlog.Eventlog, storage storage.Backend, downloader module.Downloader) error {
	var errors error
	for _, added := range diff.Added {
		if err := add(ctx, added, originURL, eLog, storage, downloader); err != nil {
			errors = multierror.Append(errors, err)
		}
	}
	for _, deprecated := range diff.Deprecated {
		if err := deprecate(ctx, deprecated, originURL, eLog, storage); err != nil {
			errors = multierror.Append(errors, err)
		}
	}
	for _, deleted := range diff.Deleted {
		if err := delete(deleted, eLog, storage); err != nil {
			errors = multierror.Append(errors, err)
		}
	}
	return errors
}

func add(ctx context.Context, event eventlog.Event, originURL string, eLog eventlog.Eventlog, storage storage.Backend, downloader module.Downloader) error {
	if _, err := eLog.ReadSingle(event.Module, event.Version); err != nil {
		// the module/version already exists, is deprecated, or is
		// tombstoned, so nothing to do
		return err
	}

	// download code from the origin
	data, err := downloader(ctx, env.Timeout(), originURL, event.Module, event.Version)
	if err != nil {
		log.Printf("error downloading new module %s/%s from %s (%s)", event.Module, event.Version, originURL, err)
		return err
	}
	defer data.Zip.Close()
	// save module data to the CDN
	if err := storage.Save(ctx, event.Module, event.Version, data.Mod, data.Zip, data.Info); err != nil {
		log.Printf("error saving new module %s/%s to CDN (%s)", event.Module, event.Version, err)
		return err
	}

	// save module metadata to the key/value store
	if _, err := eLog.Append(eventlog.Event{Module: event.Module, Version: event.Version, Time: time.Now(), Op: eventlog.OpAdd}); err != nil {
		log.Printf("error saving metadata for new module %s/%s (%s)", event.Module, event.Version, err)
		return err
	}
	return nil
}

func deprecate(ctx context.Context, event eventlog.Event, originURL string, eLog eventlog.Eventlog, storage storage.Backend) error {
	fromDB, err := eLog.ReadSingle(event.Module, event.Version)
	if err != nil {
		log.Printf("error getting event module %s/%s (%s)", event.Module, event.Version, err)
		return err
	}
	if fromDB.Op == eventlog.OpDel {
		return err // can't deprecate something that's already deleted
	}
	// delete from the CDN
	if err := storage.Delete(event.Module, event.Version); err != nil {
		log.Printf("error deleting event module %s/%s from CDN (%s)", event.Module, event.Version, err)
		return err
	}

	// add the tombstone to module metadata
	if _, err := eLog.Append(eventlog.Event{Module: event.Module, Version: event.Version, Time: time.Now(), Op: eventlog.OpDel}); err != nil {
		log.Printf("error saving metadata for deprecated module %s/%s from CDN (%s)", event.Module, event.Version, err)
		return err
	}
	return nil
}

func delete(event eventlog.Event, eLog eventlog.Eventlog, storage storage.Backend) error {
	// delete in the CDN
	if err := storage.Delete(event.Module, event.Version); err != nil {
		log.Printf("error deleting event module %s/%s from CDN (%s)", event.Module, event.Version, err)
		return err
	}
	// add tombstone to module metadata
	if _, err := eLog.Append(eventlog.Event{Module: event.Module, Version: event.Version, Time: time.Now(), Op: eventlog.OpDel}); err != nil {
		log.Printf("error inserting tombstone for deleted module %s/%s (%s)", event.Module, event.Version, err)
		return err
	}
	return nil
}
