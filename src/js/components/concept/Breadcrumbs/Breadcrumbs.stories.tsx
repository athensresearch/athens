import React from 'react';
import { BADGE, Storybook } from '@/utils/storybook';
import { Breadcrumbs } from './Breadcrumbs';

export default {
  title: 'Concepts/Breadcrumbs',
  component: Breadcrumbs,
  argTypes: {},
  decorators: [(Story) => <Storybook.Wrapper>{Story()}</Storybook.Wrapper>],
  parameters: {
    badges: [BADGE.DEV]
  }
};

export const Basic = () => {
  return (
    <Breadcrumbs>
      <Breadcrumbs.Item href="#">Item 1</Breadcrumbs.Item>
      <Breadcrumbs.Item href="#">Item 2</Breadcrumbs.Item>
      <Breadcrumbs.Item href="#">Item 3</Breadcrumbs.Item>
    </Breadcrumbs>
  );
};

export const SingleItem = () => {
  return (
    <Breadcrumbs>
      <Breadcrumbs.Item href="#">Item</Breadcrumbs.Item>
    </Breadcrumbs>
  );
};

export const LotsOfItems = () => {
  const items = "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum.".split(' ');

  return (
    <Breadcrumbs>
      {items.map((item, index) => (
        <Breadcrumbs.Item href="#" key={item + index}>{item}</Breadcrumbs.Item>
      ))}
    </Breadcrumbs>
  );
};

export const Children = () => {
  return (
    <Breadcrumbs>
      <h5>Heading</h5>
      <h6>Heading</h6>
      <p>Heading</p>
    </Breadcrumbs>
  );
};