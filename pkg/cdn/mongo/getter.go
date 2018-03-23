package mongo

import (
	"github.com/globalsign/mgo"
	"github.com/globalsign/mgo/bson"
)

type getter struct {
	conn *mgo.Session
	db   string
	coll string
}

func (g *getter) Get(module string) (string, error) {
	coll := g.conn.DB(g.db).C(g.coll)
	params := bson.M{"module": module}
	entry := Entry{}
	if err := coll.Find(params).One(&entry); err != nil {
		return "", nil
	}
	return entry.RedirectURL, nil
}
