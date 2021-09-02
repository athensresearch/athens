import React from 'react';
import { Avatar } from './Avatar';
import styled from 'styled-components';
import { BADGE, Storybook } from '../../storybook';
import { WithAvatars } from '../Block/Block.stories';

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
  name: 'Jeff',
  size: "3rem",
  color: '#0000f7',
};

export const Sizes = () => (
  <>
    <Avatar name="Jeff" color="#000f7a" size="1em" />
    <Avatar name="Jeff" color="#000f7a" size="2em" />
    <Avatar name="Jeff" color="#000f7a" size="3em" />
  </>
)

export const Colors = () => (
  <>
    <Avatar name="Jeff" color="#DDA74C" size="3em" />
    <Avatar name="Jeff" color="#C45042" size="3em" />
    <Avatar name="Jeff" color="#611A58" size="3em" />
    <Avatar name="Jeff" color="#21A469" size="3em" />
    <Avatar name="Jeff" color="#0062BE" size="3em" />
    <Avatar name="Jeff" color="#009FB8" size="3em" />
  </>
)

export const OnBlocks = WithAvatars;
OnBlocks.parameters = {
  layout: 'fullscreen'
}
