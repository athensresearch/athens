import { mockPeople } from '../../Avatar/mockData';

import { Block } from './Block';
import { BADGE, Storybook } from '../../../storybook';
import { Meter } from '../Meter';
import { blockTree } from './mockData';
import { renderBlocks } from './utils/renderBlocks';
import { useToggle } from './hooks/useToggle';
import { usePresence } from './hooks/usePresence';
import { useChecklist } from './hooks/useChecklist';
import { useSelection } from './hooks/useSelection';
import { useBlockState } from './hooks/useBlockState';
import { usePresenceProvider } from './hooks/usePresenceProvider';

const mockPresence = mockPeople.map((p, index) => ({ ...p, uid: index.toString() }))

export default {
  title: 'Concepts/Block',
  blockComponent: Block,
  argTypes: {},
  decorators: [(Story) => <Storybook.Wrapper><Story /></Storybook.Wrapper>],
  parameters: {
    badges: [BADGE.DEV]
  }
};

const Template = (args) => <Block {...args} />;

export const Basic = Template.bind({});
Basic.args = {
  ...blockTree.blocks["1"],
};

export const Editing = Template.bind({});
Editing.args = {
  ...blockTree.blocks["1"],
  isEditing: true,
};

export const References = Template.bind({});
References.args = {
  ...blockTree.blocks["1"],
  refsCount: 12
};

export const Selected = Template.bind({});
Selected.args = {
  ...blockTree.blocks["1"],
  isSelected: true,
};

export const WithToggle = () => {
  const { blockGraph: withState, setBlockState } = useBlockState(blockTree);
  const { blockGraph } = useToggle(withState, setBlockState);

  const blocks = renderBlocks({
    blockGraph: blockGraph,
    setBlockState: setBlockState,
    blockComponent: <Block />
  });

  return blocks;
}


export const WithPresence = () => {
  const { blockGraph: withState, setBlockState: withStateState } = useBlockState(blockTree);
  const { blockGraph, setBlockState } = usePresence(withState, withStateState);

  const blocks = renderBlocks({
    blockGraph: blockGraph,
    setBlockState: setBlockState,
    blockComponent: <Block />
  });

  return blocks;
}

WithPresence.decorators = [(Story) => {
  const { PresenceProvider, clearPresence } = usePresenceProvider({ presentPeople: mockPresence });

  return <Storybook.Wrapper>
    <PresenceProvider>
      <button onClick={() => clearPresence()}>Clear Presence</button>
      <Story />
    </PresenceProvider>
  </Storybook.Wrapper>
}];

export const WithChecklist = () => {
  const { blockGraph: withState, setBlockState } = useBlockState(blockTree);
  const { blockGraph: withToggle } = useToggle(withState, setBlockState);
  const { blockGraph, checked, total } = useChecklist(withToggle, setBlockState);

  const blocks = renderBlocks({
    blockGraph: blockGraph,
    setBlockState: setBlockState,
    blockComponent: <Block />
  });

  return <>
    <Meter value={checked} maxValue={total} label="Completed" />
    <br />
    {blocks}
  </>;
}

export const WithSelection = () => {
  const { blockGraph: withState, setBlockState } = useBlockState(blockTree);
  const { blockGraph: withToggle } = useToggle(withState, setBlockState);
  const { blockGraph } = useSelection(withToggle, setBlockState);

  const blocks = renderBlocks({
    blockGraph: blockGraph,
    setBlockState: setBlockState,
    blockComponent: <Block />
  });

  return blocks;
}

export const MultipleSelected = () => {
  const { blockGraph: withState, setBlockState } = useBlockState(blockTree);
  const { blockGraph: withToggle } = useToggle(withState, setBlockState);
  const { blockGraph } = useSelection(withToggle, setBlockState, true);

  const blocks = renderBlocks({
    blockGraph: blockGraph,
    setBlockState: setBlockState,
    blockComponent: <Block />
  });

  return blocks;
}
