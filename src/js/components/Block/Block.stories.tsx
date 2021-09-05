import React from 'react';

import { Block } from './Block';
import { BADGE, Storybook } from '../../storybook';
import { Checklist } from '../Checkbox/Checkbox.stories';
import { block, blockTree, blockTreeSeries, blockTreeWithAvatars } from './mockData';

export default {
  title: 'Components/Block',
  component: Block,
  argTypes: {},
  decorators: [(Story) => <Storybook.Wrapper><Story /></Storybook.Wrapper>],
  parameters: {
    badges: [BADGE.DEV]
  }
};

const toggleBlockOpen = (blockUid, setBlockState) => {
  setBlockState(prevState => {
    console.log(prevState);
    return ({
      ...prevState,
      blocks: {
        ...prevState.blocks,
        [blockUid]: {
          ...prevState.blocks[blockUid],
          isOpen: !prevState.blocks[blockUid].isOpen
        }
      }
    })
  })
}

const Template = (args) => <Block {...args} />;

export const Basic = Template.bind({});
Basic.args = {
  ...block,
};

export const Editing = Template.bind({});
Editing.args = {
  ...block,
  isEditing: true,
};

const recurseBlocks = (tree: any[], content: Object, setBlockState, blockProps) => {
  return tree.map(block => {
    return (
      <Block
        {...content[block.uid]}
        {...blockProps}
      >
        {block.children && recurseBlocks(block.children, content, setBlockState, blockProps)}
      </Block>
    )
  })
};

export const BlockTree = () => {
  const [blockState, setBlockState] = React.useState(blockTree);
  return recurseBlocks(
    blockState.tree,
    blockState.blocks,
    setBlockState,
    {
      handlePressToggle: () => toggleBlockOpen(block.uid, setBlockState)
    })
}

export const Series = () => {
  const [blockState, setBlockState] = React.useState(blockTreeSeries);
  return recurseBlocks(
    blockState.tree,
    blockState.blocks,
    setBlockState,
    {
      handlePressToggle: () => toggleBlockOpen(block.uid, setBlockState)
    })
}

export const WithPresence = () => {
  const [blockState, setBlockState] = React.useState(blockTreeWithAvatars);
  return recurseBlocks(
    blockState.tree,
    blockState.blocks,
    setBlockState,
    {
      handlePressToggle: () => toggleBlockOpen(block.uid, setBlockState)
    })
}

export const References = Template.bind({});
References.args = {
  ...block,
  refsCount: 12
};

export const Selected = Template.bind({});
Selected.args = {
  ...block,
  isSelected: true,
};

export const MultipleSelected = () => {
  const [blockState, setBlockState] = React.useState(blockTree);
  return recurseBlocks(blockState.tree,
    blockState.blocks,
    setBlockState,
    {
      isSelected: true,
      handlePressToggle: () => toggleBlockOpen(block.uid, setBlockState)
    }
  )
}

export const ChecklistBlock = () => {
  return <Block
    uid="123"
    rawContent=""
    renderedContent={<Checklist />}
    isOpen={true}
    isEditable={false}
  />
}