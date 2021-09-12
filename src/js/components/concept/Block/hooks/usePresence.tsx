import React from 'react';
import { modifyBlocks } from '../utils/modifyBlocks';
import { toggleBlockProperty } from '../utils/toggleBlockProperty';
import { BlockGraph } from '../../../../main';

export const usePresence = (blockGraph: BlockGraph, setBlockState, presence) => {
  const [presenceState, setPresenceState] = React.useState(presence);

  const resultGraph = modifyBlocks({
    blockGraph: blockGraph,
    ApplyProps: (block) => ({
      ...block,
      presentUser: presenceState.find((p: PersonPresence) => p.uid === block.uid),
      handlePressToggle: (uid: UID) => toggleBlockProperty(uid, 'isOpen', setBlockState),
    }),
  });

  return { blockGraph: resultGraph, setBlockState, setPresence: setPresenceState };
};
