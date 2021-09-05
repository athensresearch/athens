
export const synced = false;

export const allDbs = [{
  name: 'Athens Research',
  id: 'location of db',
  "is-remote": false,
},
{
  name: 'Top Secret',
  id: 'location of db',
  "is-remote": false,
},
{
  name: 'Pokemon',
  id: 'location of db',
  "is-remote": false,
}];

export const activeDb = allDbs[0];

export const inactiveDbs = allDbs.slice(1);

export const examples = {
  "long db names": {
    inactiveDbs: [{
      name: 'Lorem Ipsum Dolor Sit Amet Donec Consectetur',
      id: 'location of db',
      "is-remote": false,
    }, {
      name: 'Dolor Sit Amet Donec Consectetur Lorem Ipsum',
      id: 'location of db',
      "is-remote": true,
    },
    {
      name: 'Lorem Ipsum Dolor Sit Amet Donec Consectetur',
      id: 'location of db',
      "is-remote": false,
    }],
    activeDb: {
      name: 'Dolor Sit Amet Donec Consectetur Lorem Ipsum',
      id: 'location of db',
      "is-remote": false,
    }
  },
  "no other dbs": {
    inactiveDbs: [],
    activeDb: {
      name: 'Athens Research',
      id: 'location of db',
      "is-remote": false,
    }
  },
  "lots of dbs": {
    inactiveDbs: [{
      name: 'Lorem Ipsum Dolor Sit Amet Donec Consectetur',
      id: 'location of db',
      "is-remote": false,
    }, {
      name: 'Dolor Sit Amet Donec Consectetur Lorem Ipsum',
      id: 'location of db',
      "is-remote": true,
    },
    {
      name: 'Lorem Ipsum Dolor Sit Amet Donec Consectetur',
      id: 'location of db',
      "is-remote": false,
    }, {
      name: 'Dolor Sit Amet Donec Consectetur Lorem Ipsum',
      id: 'location of db',
      "is-remote": true,
    },
    {
      name: 'Lorem Ipsum Dolor Sit Amet Donec Consectetur',
      id: 'location of db',
      "is-remote": false,
    }, {
      name: 'Dolor Sit Amet Donec Consectetur Lorem Ipsum',
      id: 'location of db',
      "is-remote": true,
    },
    {
      name: 'Lorem Ipsum Dolor Sit Amet Donec Consectetur',
      id: 'location of db',
      "is-remote": false,
    }, {
      name: 'Dolor Sit Amet Donec Consectetur Lorem Ipsum',
      id: 'location of db',
      "is-remote": true,
    },
    {
      name: 'Lorem Ipsum Dolor Sit Amet Donec Consectetur',
      id: 'location of db',
      "is-remote": false,
    },
    {
      name: 'Lorem Ipsum Dolor Sit Amet Donec Consectetur',
      id: 'location of db',
      "is-remote": false,
    }, {
      name: 'Dolor Sit Amet Donec Consectetur Lorem Ipsum',
      id: 'location of db',
      "is-remote": true,
    },
    {
      name: 'Lorem Ipsum Dolor Sit Amet Donec Consectetur',
      id: 'location of db',
      "is-remote": false,
    }],
    activeDb: {
      name: 'Athens Research',
      id: 'location of db',
      "is-remote": false,
    }
  }
}