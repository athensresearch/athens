import React from 'react';
import { Page } from '../Page';
import { Block } from '../Block';
import styled from 'styled-components';
import { BADGE, Storybook } from '../../storybook';
import { DateTime } from 'luxon';
import { Welcome } from '../Block/Block.stories';
import { preferredDateFormat } from '../../config';

// Helpers

const data = {
  randomBlockContent: [
    "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum.",
    "Donec id elit non mi porta gravida at eget metus. Maecenas sed diam eget risus varius blandit sit amet non magna. Cras justo odio, dapibus ac facilisis in, egestas eget quam. Donec id elit non mi porta gravida at eget metus. Nullam id dolor id nibh ultricies vehicula ut id elit. Cras justo odio, dapibus ac facilisis in, egestas eget quam. Donec id elit non mi porta gravida at eget metus. Nullam id dolor id nibh ultricies vehicula ut id elit. Cras justo odio, dapibus ac facilisis in, egestas eget quam. Donec id elit non mi porta gravida at eget metus. Nullam id dolor id nibh ultricies vehicula ut id elit. Cras justo odio, dapibus ac facilisis in, egestas eget quam. Donec id elit non mi porta gravida at eget metus. Nullam id dolor id nibh ultricies vehicula ut id elit. Cras justo odio, dapibus ac facilisis in, egestas eget quam. Donec id elit non mi porta gravida at eget metus. Nullam id dolor id nibh ultricies vehicula ut id elit. Cras justo odio, dapibus ac facilisis in, egestas eget quam. Donec id elit non mi porta gravida at eget metus. Nullam id dolor id nibh ultricies vehicula ut id elit. Cras justo odio, dapibus ac facilisis in, egestas eget quam. Donec id elit non mi porta gravida at eget metus. Nullam id dolor id nibh ultricies vehicula ut id elit. Cras justo odio, dapibus ac facilisis in, egestas eget quam. Donec id elit non mi porta gravida at.",
    "Consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum."
  ],
  contentWithLink: () => <>regular text <a href="https://www.google.com">link</a> regular text</>,
};


export default {
  title: 'Components/Page',
  component: Page,
  argTypes: {},
  parameters: {
    badges: [BADGE.DEV]
  },
};

// Stories

const Template = (args) => <Page {...args} />;

export const NodePage = Template.bind({});
NodePage.args = {
  isDailyNote: false,
  title: 'Node Page',
  uid: '123'
};

export const BlockPage = Template.bind({});
BlockPage.args = {
  isDailyNote: false,
  title: 'Block Page',
  uid: '123'
};

export const BlockPageWithLongTitle = Template.bind({});
BlockPageWithLongTitle.args = {
  isDailyNote: false,
  title: 'Lorem ipsum dolor sit amet donec consectetur',
  uid: '123'
};

export const DailyNote = Template.bind({});
DailyNote.args = {
  isDailyNote: true,
  hasShortcut: false,
  title: DateTime.now().toLocaleString(preferredDateFormat),
  uid: DateTime.now().toISODate()
};

export const WelcomePage = () => {
  return (
    <Page
      isDailyNote={false}
      hasShortcut={true}
      title="Welcome"
      uid="123"
    >
      <Welcome />
    </Page>
  );
}