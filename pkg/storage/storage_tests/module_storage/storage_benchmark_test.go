package modulestorage

import (
	"bytes"
	"context"
	"fmt"
	"testing"

	"github.com/gobuffalo/suite"
	"github.com/gomods/athens/pkg/errors"
	"github.com/gomods/athens/pkg/storage"
	"github.com/gomods/athens/pkg/storage/fs"
	"github.com/gomods/athens/pkg/storage/mem"
	"github.com/gomods/athens/pkg/storage/minio"
	"github.com/gomods/athens/pkg/storage/mongo"
	"github.com/stretchr/testify/require"
)

func BenchmarkStorageList(b *testing.B) {
	module, version := "athens_module", "1.0.1"
	zip, info, mod := []byte("zip_data"), []byte("info"), []byte("mod_info")

	for _, store := range getStores(b) {

		backend := store.Storage()

		require.NoError(b, backend.Save(context.Background(), module, version, mod, bytes.NewReader(zip), info), "Save for storage %s failed", backend)

		b.ResetTimer()
		b.Run(store.StorageHumanReadableName(), func(b *testing.B) {
			for i := 0; i < b.N; i++ {
				_, err := backend.List(context.Background(), module)
				require.NoError(b, err, "Error in listing module")
			}
		})

		require.NoError(b, store.Cleanup())
	}
}

func BenchmarkStorageSave(b *testing.B) {
	module, version := "athens_module", "1.0.1"
	zip, info, mod := []byte("zip_data"), []byte("info"), []byte("mod_info")

	for _, store := range getStores(b) {

		backend := store.Storage()

		b.ResetTimer()
		mi := 0
		b.Run(store.StorageHumanReadableName(), func(b *testing.B) {
			for i := 0; i < b.N; i++ {
				err := backend.Save(context.Background(), fmt.Sprintf("save-%s-%d", module, mi), version, mod, bytes.NewReader(zip), info)
				require.NoError(b, err)
				mi++
			}
		})

		require.NoError(b, store.Cleanup())
	}
}

func BenchmarkStorageSaveAndDelete(b *testing.B) {
	module, version := "athens_module", "1.0.1"
	zip, info, mod := []byte("zip_data"), []byte("info"), []byte("mod_info")

	for _, store := range getStores(b) {

		backend := store.Storage()

		b.ResetTimer()
		b.Run(store.StorageHumanReadableName(), func(b *testing.B) {
			for i := 0; i < b.N; i++ {
				name := fmt.Sprintf("del-%s-%d", module, i)
				err := backend.Save(context.Background(), name, version, mod, bytes.NewReader(zip), info)
				require.NoError(b, err, "save for storage %s module: %s failed", backend, name)
				err = backend.Delete(context.Background(), name, version)
				require.NoError(b, err, "delete failed: %s", name)
			}
		})

		require.NoError(b, store.Cleanup())
	}
}

func BenchmarkStorageDeleteNonExistingModules(b *testing.B) {
	module, version := "random-module", "version"
	for _, store := range getStores(b) {
		backend := store.Storage()

		b.ResetTimer()
		b.Run(store.StorageHumanReadableName(), func(b *testing.B) {
			for i := 0; i < b.N; i++ {
				err := backend.Delete(context.Background(), fmt.Sprintf("del-%s-%d", module, i), version)
				require.Equal(b, errors.KindNotFound, errors.Kind(err))
			}
		})
	}
}

func BenchmarkStorageExists(b *testing.B) {
	module, version := "athens_module", "1.0.1"
	zip, info, mod := []byte("zip_data"), []byte("info"), []byte("mod_info")
	moduleName := fmt.Sprintf("existing-%s", module)

	for _, store := range getStores(b) {
		backend := store.Storage()
		err := backend.Save(context.Background(), moduleName, version, mod, bytes.NewReader(zip), info)
		require.NoError(b, err, "exists for storage %s failed", backend)

		b.ResetTimer()
		b.Run(store.StorageHumanReadableName(), func(b *testing.B) {
			for i := 0; i < b.N; i++ {
				exists, err := backend.Exists(context.Background(), moduleName, version)
				require.NoError(b, err)
				require.True(b, exists)
			}
		})

		require.NoError(b, store.Cleanup())
	}
}

func getStores(b *testing.B) []storage.TestSuite {
	var stores []storage.TestSuite

	//TODO: create the instance without model or TestSuite
	model := suite.NewModel()
	fsStore, err := fs.NewTestSuite(model)
	require.NoError(b, err, "couldn't create filesystem store")
	stores = append(stores, fsStore)

	mongoStore, err := mongo.NewTestSuite(model)
	require.NoError(b, err, "couldn't create mongo store")
	stores = append(stores, mongoStore)

	memStore, err := mem.NewTestSuite(model)
	require.NoError(b, err)
	stores = append(stores, memStore)

	minioStore, err := minio.NewTestSuite(model)
	require.NoError(b, err)
	stores = append(stores, minioStore)

	return stores
}
