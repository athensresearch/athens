import React from 'react';
import { PersonPresence } from '../../../../main';
import { mockPeople } from '../../../Avatar/mockData';

export const PresenceContext = React.createContext(null);

interface usePresenceProviderProps {
  presentPeople: PersonPresence[];
}

export const usePresenceProvider = ({ presentPeople }: usePresenceProviderProps) => {
  const [presence, setPresence] = React.useState<PersonPresence[]>(presentPeople);

  const randomPerson = () => mockPeople[Math.floor(Math.random() * mockPeople.length)];
  const numberOfBlocks = 6

  const clearPresence = () => setPresence([]);
  const fillPresence = () => setPresence(presence.slice(0, numberOfBlocks + 1));
  const removePresence = () => setPresence(presence.slice(0, presence.length - 1));
  const addPresence = () => setPresence([...presence, { ...randomPerson(), uid: Math.ceil(Math.random() * numberOfBlocks).toString() }]);

  const context = {
    presence,
    clearPresence,
    fillPresence,
    removePresence,
    addPresence,
  };

  const PresenceProvider = ({ children }) =>
    <PresenceContext.Provider value={context}>
      {children}
    </PresenceContext.Provider>;

  return {
    PresenceProvider: PresenceProvider,
    PresenceContext: PresenceContext,
    presence,
    setPresence,
    clearPresence
  }
}

