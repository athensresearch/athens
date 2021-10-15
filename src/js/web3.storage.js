import "regenerator-runtime/runtime";
// shadow-cljs doesn't support some of the import formats here, so we use the bundle.
// See https://github.com/web3-storage/web3.storage/issues/260#issuecomment-894171368
import { Web3Storage } from 'web3.storage/dist/bundle.esm.min.js';

function token() {
  // key goes here
  return "";
}

function client() {
  return new Web3Storage({ token: token() });
}

const filename = "events.edn";

export async function put(name, str) {
  console.log("--web3 put name", name, "str:", str);
  const c = client();
  const files = [new File([str], filename)];
  const cid = await c.put(files, {name});
  console.log('stored files with cid:', cid);
  return cid;
}

export async function get(cid) {
  console.log("--web3 get cid", cid);
  const c = client();
  const res = await c.get(cid);
  console.log(`Got a response! [${res.status}] ${res.statusText}`);
  if (!res.ok) {
    throw new Error(`failed to get ${cid} - [${res.status}] ${res.statusText}`);
  }

  // unpack File objects from the response
  const files = await res.files();
  const events = [];
  for (const file of files) {
    if (file.name == filename){
      return await file.text();
    }
    // console.log(file);
  }

  return null;
}

// Without better filtering options for we mostly have to get the full list.
// `maxResults` allows pagination, but with `before` means you always have to start at the
// most recent and hold them all in memory while you work backwards.
// Option wishlisht: `after`, `between`, `namePrefix`.
export async function list(prefix) {
  const c = client();
  const uploads = [];
  // There's currently no good way of handling async iterables in cljs, so might
  // as well use plain js and return a single promise.
  for await (const upload of c.list()) {
    if (upload.name.startsWith(prefix)) {
      // I don't think the timestamps are unique, would have to tiebreak with uid or something.
      uploads.push(upload.cid);
      // console.log(`${upload.name} - cid: ${upload.cid} - size: ${upload.dagSize}`);
    }
  }

  // console.log(uploads);
  // newest to oldest
  return uploads;
}

function timeout(ms) {
  return new Promise(resolve => setTimeout(resolve, ms));
}

export async function listen(prefix, cb){
  console.log("--web3 listen prefix", prefix);
  let lastCid = null;

  while (true) {
    // newest to oldest
    const cids = await list(prefix);
    const eventsUntilCid = [];

    for (const cid of cids) {
      if (cid == lastCid) {
        break;
      }

      const event = await get(cid);
      if (event) {
        eventsUntilCid.unshift(event);
      }
    }

    if (eventsUntilCid.length > 0 || lastCid == null) {
      console.log("--web3 listen cb", eventsUntilCid);
      // oldest to newest.
      cb(eventsUntilCid);
      lastCid = cids[0];
    }

    await timeout(5000);
  }
}
