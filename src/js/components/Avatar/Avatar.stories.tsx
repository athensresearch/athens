import React from 'react';
import { Avatar } from './Avatar';
import styled from 'styled-components';
import { BADGE, Storybook } from '../../storybook';
import { WithPresence } from '../Block/Block.stories';

const Wrapper = styled(Storybook.Wrapper)`
  display: flex;
  gap: 1rem;
`;

// Helpers

export default {
  title: 'Components/Avatar',
  component: Avatar,
  argTypes: {},
  parameters: {
    layout: 'centered',
    badges: [BADGE.DEV]
  },
  decorators: [(Story) => <Wrapper><Story /></Wrapper>]
};

// Stories

const Template = (args) => <Avatar {...args} />;

export const Basic = Template.bind({});
Basic.args = {
  username: 'Jeff Tang',
  size: "3rem",
  color: '#0000f7',
};

export const WithTooltip = () => (
  <div style={{ display: 'flex', gap: "2rem", justifyContent: "space-between", width: "30rem" }}>
    <Avatar personId="1" username="Jeff Tang" color="#000f7a" size="2em" showTooltip={true} tooltipPlacement="top" />
    <Avatar personId="1" username="Jeff Tang" color="#000f7a" size="2em" showTooltip={true} tooltipPlacement="right" />
    <Avatar personId="1" username="Jeff Tang" color="#000f7a" size="2em" showTooltip={true} tooltipPlacement="bottom" />
    <Avatar personId="1" username="Jeff Tang" color="#000f7a" size="2em" showTooltip={true} tooltipPlacement="left" />
  </div>
)

export const Sizes = () => (
  <div style={{ display: "flex", alignItems: "center", gap: "2rem" }}>
    <Avatar personId="1" username="Jeff Tang" color="#000f7a" size="1em" />
    <Avatar personId="1" username="Jeff Tang" color="#000f7a" size="2em" />
    <Avatar personId="1" username="Jeff Tang" color="#000f7a" size="3em" />
    <Avatar personId="1" username="Jeff Tang" color="#000f7a" size="4em" />
  </div>
)

export const Colors = () => (
  <>
    <Avatar personId="1" username="Jeff Tang" color="#DDA74C" size="3em" />
    <Avatar personId="1" username="Jeff Tang" color="#C45042" size="3em" />
    <Avatar personId="1" username="Jeff Tang" color="#611A58" size="3em" />
    <Avatar personId="1" username="Jeff Tang" color="#21A469" size="3em" />
    <Avatar personId="1" username="Jeff Tang" color="#0062BE" size="3em" />
    <Avatar personId="1" username="Jeff Tang" color="#009FB8" size="3em" />
  </>
)

export const isMuted = () => (
  <>
    <Avatar personId="1" isMuted={true} username="Jeff Tang" color="#DDA74C" size="3em" />
    <Avatar personId="1" isMuted={true} username="Jeff Tang" color="#C45042" size="3em" />
    <Avatar personId="1" isMuted={true} username="Jeff Tang" color="#611A58" size="3em" />
    <Avatar personId="1" isMuted={true} username="Jeff Tang" color="#21A469" size="3em" />
    <Avatar personId="1" isMuted={true} username="Jeff Tang" color="#0062BE" size="3em" />
    <Avatar personId="1" isMuted={true} username="Jeff Tang" color="#009FB8" size="3em" />
  </>
)

export const Stack = () => (
  <Avatar.Stack>
    <Avatar personId="1" username="Jeff Tang" color="#DDA74C" tooltipPlacement="bottom" size="3em" />
    <Avatar personId="1" username="Jeff Tang" color="#C45042" tooltipPlacement="bottom" size="3em" />
    <Avatar personId="1" username="Jeff Tang" color="#611A58" tooltipPlacement="bottom" size="3em" />
    <Avatar personId="1" username="Jeff Tang" color="#21A469" tooltipPlacement="bottom" size="3em" />
    <Avatar personId="1" username="Jeff Tang" color="#0062BE" tooltipPlacement="bottom" size="3em" />
    <Avatar personId="1" username="Jeff Tang" color="#009FB8" tooltipPlacement="bottom" size="3em" />
  </Avatar.Stack>
)

export const StackNarrow = () => (
  <Avatar.Stack overlap={0.25}>
    <Avatar personId="1" username="Jeff Tang" color="#DDA74C" tooltipPlacement="bottom" size="3em" />
    <Avatar personId="1" username="Jeff Tang" color="#C45042" tooltipPlacement="bottom" size="3em" />
    <Avatar personId="1" username="Jeff Tang" color="#611A58" tooltipPlacement="bottom" size="3em" />
    <Avatar personId="1" username="Jeff Tang" color="#21A469" tooltipPlacement="bottom" size="3em" />
    <Avatar personId="1" username="Jeff Tang" color="#0062BE" tooltipPlacement="bottom" size="3em" />
    <Avatar personId="1" username="Jeff Tang" color="#009FB8" tooltipPlacement="bottom" size="3em" />
  </Avatar.Stack>
)
export const StackWide = () => (
  <Avatar.Stack overlap={0.75}>
    <Avatar personId="1" username="Jeff Tang" color="#DDA74C" tooltipPlacement="bottom" size="3em" />
    <Avatar personId="1" username="Jeff Tang" color="#C45042" tooltipPlacement="bottom" size="3em" />
    <Avatar personId="1" username="Jeff Tang" color="#611A58" tooltipPlacement="bottom" size="3em" />
    <Avatar personId="1" username="Jeff Tang" color="#21A469" tooltipPlacement="bottom" size="3em" />
    <Avatar personId="1" username="Jeff Tang" color="#0062BE" tooltipPlacement="bottom" size="3em" />
    <Avatar personId="1" username="Jeff Tang" color="#009FB8" tooltipPlacement="bottom" size="3em" />
  </Avatar.Stack>
)

export const OnBlocks = WithPresence;