import React from 'react';
import { modifyBlocks } from '../utils/modifyBlocks';
import { toggleBlockProperty } from '../utils/toggleBlockProperty';
import { PresenceContext } from './usePresenceProvider';

export const usePresence = (blockGraph: BlockGraph, setBlockState) => {
  const { presence } = React.useContext(PresenceContext);

  const resultGraph = modifyBlocks({
    blockGraph: blockGraph,
    ApplyProps: (block) => ({
      ...block,
      presentUser: presence.find((p: PersonPresence) => p.uid === block.uid),
      handlePressToggle: (uid: UID) => toggleBlockProperty(uid, 'isOpen', setBlockState),
    }),
  });

  return { blockGraph: resultGraph, setBlockState };
};
