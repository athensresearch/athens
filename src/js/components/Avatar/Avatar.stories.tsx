import React from 'react';
import { Avatar } from './Avatar';
import styled from 'styled-components';

// Helpers

const StoryWrapper = styled.div`
  margin: 4rem;
  display: flex;
  gap: 1rem;
`;

export default {
  title: 'Components/Avatar',
  component: Avatar,
  argTypes: {},
  parameters: {
    layout: 'centered'
  }
};

// Stories

const Template = (args) => <StoryWrapper><Avatar {...args} /></StoryWrapper>;

export const Basic = Template.bind({});
Basic.args = {
  name: 'Jeff',
  size: "3rem",
  color: '#0000f7',
};

export const Sizes = () => (
  <StoryWrapper>
    <Avatar name="Jeff" color="#000f7a" size="1em" />
    <Avatar name="Jeff" color="#000f7a" size="2em" />
    <Avatar name="Jeff" color="#000f7a" size="3em" />
  </StoryWrapper>
)

export const Colors = () => (
  <StoryWrapper>
    <Avatar name="Jeff" color="#DDA74C" size="3em" />
    <Avatar name="Jeff" color="#C45042" size="3em" />
    <Avatar name="Jeff" color="#611A58" size="3em" />
    <Avatar name="Jeff" color="#21A469" size="3em" />
    <Avatar name="Jeff" color="#0062BE" size="3em" />
    <Avatar name="Jeff" color="#009FB8" size="3em" />
  </StoryWrapper>
)