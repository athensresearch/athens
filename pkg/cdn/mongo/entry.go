package mongo

// Entry is stored in the DB. Right now it just holds a redirect URL to
// the CDN
type Entry struct {
	Module      string `bson:"module"`
	RedirectURL string `bson:"redirect_url"`
	// Other fields?
}
