import { modifyBlocks } from '../utils/modifyBlocks';
import { toggleBlockProperty } from '../utils/toggleBlockProperty';
import { BlockGraph } from '../../../../main';

export const useSelection = (blockGraph: BlockGraph, setBlockState, defaultSelected = undefined) => {

  const graph = modifyBlocks({
    blockGraph: blockGraph,
    ApplyProps: (block) => ({
      isSelected: block.isSelected ? block.isSelected : defaultSelected,
      handlePressContainer: () => toggleBlockProperty(block.uid, 'isSelected', setBlockState),
    }),
  });

  return { blockGraph: graph };
};
