import { people } from './people';

export const peoplePresence = people.map((p, index) => ({ ...p, uid: index.toString() }))