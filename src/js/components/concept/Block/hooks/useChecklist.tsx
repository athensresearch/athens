
import { Checkbox } from '@/concept/Checkbox';
import { modifyBlocks } from '../utils/modifyBlocks';
import { toggleBlockProperty } from '../utils/toggleBlockProperty';

export const useChecklist = (blockGraph: BlockGraph, setBlockState) => {
  const total = Object.keys(blockGraph.blocks).length;
  const checked = Object.keys(blockGraph.blocks).filter(uid => blockGraph.blocks[uid].isChecked).length;

  const graph = modifyBlocks({
    blockGraph: blockGraph,
    ApplyProps: (block) => ({
      isChecked: block.isChecked,
      renderedContent:
        <><Checkbox
          labelProps={{ style: { marginRight: "0.25em" } }}
          styleCircle
          defaultSelected={block.isChecked}
          onChange={() => toggleBlockProperty(block.uid, 'isChecked', setBlockState)}
        />
          {block.renderedContent}
        </>,
    })
  });

  return { blockGraph: graph, checked, total };
};
