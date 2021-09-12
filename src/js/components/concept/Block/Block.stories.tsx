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


export default {
  title: 'Components/Block',
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
  const presence = mockPeople.map((p, index) => ({ ...p, uid: index.toString() }));

  const { blockGraph: withState, setBlockState: withStateState } = useBlockState(blockTree);
  const { blockGraph, setBlockState, setPresence } = usePresence(withState, withStateState, []);

  const randomPerson = () => mockPeople[Math.floor(Math.random() * mockPeople.length)];
  const numberOfBlocks = Object.keys(blockGraph.blocks).length;

  const clearPresence = () => {
    setPresence([]);
  }

  const fillPresence = () => {
    setPresence(presence.slice(0, numberOfBlocks + 1));
  }

  const removePresence = () => {
    setPresence(prevState => [...prevState.slice(0, prevState.length - 1)]);
  }

  const addPresence = () => {
    setPresence(prevState => [...prevState,
    { ...randomPerson(), uid: Math.ceil(Math.random() * numberOfBlocks).toString() }]);
  }

  const blocks = renderBlocks({
    blockGraph: blockGraph,
    setBlockState: setBlockState,
    blockComponent: <Block />
  });

  // return <div>
  //   <button onClick={fillPresence}>Reset Presence</button>
  //   <button onClick={addPresence}>Add Presence</button>
  //   <button onClick={removePresence}>Remove Presence</button>
  //   <button onClick={clearPresence}>Clear Presence</button>

  //   {blocks}
  // </div>;

  return blocks;
}

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
