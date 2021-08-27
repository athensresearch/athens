import React from 'react';
import { Block } from './Block';
import styled from 'styled-components';

// Helpers

const toggle = (bool, setter) => {
  bool ? setter(!bool) : setter(true);
}

const blockContents = [
  "lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum.",
  "donec id elit non mi porta gravida at eget metus. Maecenas sed diam eget risus varius blandit sit amet non magna. Cras justo odio, dapibus ac facilisis in, egestas eget quam. Donec id elit non mi porta gravida at eget metus. Nullam id dolor id nibh ultricies vehicula ut id elit. Cras justo odio, dapibus ac facilisis in, egestas eget quam. Donec id elit non mi porta gravida at eget metus. Nullam id dolor id nibh ultricies vehicula ut id elit. Cras justo odio, dapibus ac facilisis in, egestas eget quam. Donec id elit non mi porta gravida at eget metus. Nullam id dolor id nibh ultricies vehicula ut id elit. Cras justo odio, dapibus ac facilisis in, egestas eget quam. Donec id elit non mi porta gravida at eget metus. Nullam id dolor id nibh ultricies vehicula ut id elit. Cras justo odio, dapibus ac facilisis in, egestas eget quam. Donec id elit non mi porta gravida at eget metus. Nullam id dolor id nibh ultricies vehicula ut id elit. Cras justo odio, dapibus ac facilisis in, egestas eget quam. Donec id elit non mi porta gravida at eget metus. Nullam id dolor id nibh ultricies vehicula ut id elit. Cras justo odio, dapibus ac facilisis in, egestas eget quam. Donec id elit non mi porta gravida at eget metus. Nullam id dolor id nibh ultricies vehicula ut id elit. Cras justo odio, dapibus ac facilisis in, egestas eget quam. Donec id elit non mi porta gravida at.",
  "consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum."
];

const BlockContainer = styled.div`
  flex: 1 1 100%;
  border: 1px solid;
  padding: 1rem;
  border-radius: 1rem;
  max-width: 900px;
`;

export default {
  title: 'Example/Block',
  component: Block,
  argTypes: {},
};

// Stories

const Template = (args) => <BlockContainer><Block {...args} /></BlockContainer>;


export const Primary = Template.bind({});
Primary.args = {
  isOpen: true,
};

export const Typical = Template.bind({});
Typical.args = {
  content: 'Block',
  children: <div><h1>test</h1>
    <h2>test test test </h2></div>,
  isOpen: true,
};

export const Series = () => {
  const [block1Open, setBlock1Open] = React.useState(true);
  const [block2Open, setBlock2Open] = React.useState(true);
  const [block3Open, setBlock3Open] = React.useState(true);

  return (<BlockContainer>
    <Block
      uid="123"
      isOpen={block1Open}
      handlePressToggle={() => toggle(block1Open, setBlock1Open)}
      content={blockContents[0]}
    />
    <Block uid="123"
      isOpen={block2Open}
      handlePressToggle={() => toggle(block2Open, setBlock2Open)}
      content={blockContents[1]}
    />
    <Block uid="123"
      isOpen={block3Open}
      handlePressToggle={() => toggle(block3Open, setBlock3Open)}
      content={blockContents[2]}
    />
  </BlockContainer>)
};

export const Tree = () => {
  const [block1Open, setBlock1Open] = React.useState(true);
  const [block2Open, setBlock2Open] = React.useState(true);
  const [block3Open, setBlock3Open] = React.useState(true);

  return (<BlockContainer>
    <Block
      uid="123"
      isOpen={block1Open}
      handlePressToggle={() => toggle(block1Open, setBlock1Open)}
      content={blockContents[0]}
    >
      <Block uid="123"
        isOpen={block2Open}
        handlePressToggle={() => toggle(block2Open, setBlock2Open)}
        content={blockContents[1]}
      >
        <Block uid="123"
          isOpen={block3Open}
          handlePressToggle={() => toggle(block3Open, setBlock3Open)}
          content={blockContents[2]}
        />
      </Block>
    </Block>
  </BlockContainer>)
};
