import { BlockProps } from '../Block';

interface modifyBlocksProps {
  blockGraph: BlockGraph;
  ApplyProps?(block: BlockProps): any;
}

export const modifyBlocks = ({
  blockGraph,
  ApplyProps = () => null,
}: modifyBlocksProps): BlockGraph => ({
  tree: blockGraph.tree,
  blocks: {
    ...Object.keys(blockGraph.blocks).map((uid) => {
      return ({
        ...blockGraph.blocks[uid],
        ...ApplyProps(blockGraph.blocks[uid]),
      })
    })
  }
})
