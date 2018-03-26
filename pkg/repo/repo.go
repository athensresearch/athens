package repo

// Crawler downloads repositories to a tmp directory, then module zip generation flow can proceed with this directory.
type Crawler interface {
	// Downloads repo to tmp folder, path to tmp returned
	DownloadRepo() (string, error)
}
