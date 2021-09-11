import React from 'react';

import { Page } from '.';
import { BADGE, Storybook } from '../../../storybook';
import { DateTime } from 'luxon';
import { preferredDateFormat } from '../../../config';

import {
  BlockTree,
  WithTasks,
  WithPresence
} from '../Block/Block.stories';

export default {
  title: 'Components/Page',
  component: Page,
  argTypes: {},
  parameters: {
    badges: [BADGE.DEV]
  },
  decorators: [(Story) => <Storybook.Wrapper>{Story()}</Storybook.Wrapper>]
};


// Stories

const Template = (args) => {
  const [isLinkedReferencesOpen, setIsLinkedReferencesOpen] = React.useState(false);
  const [isUnlinkedReferencesOpen, setIsUnlinkedReferencesOpen] = React.useState(false);
  const handlePressLinkedReferencesToggle = () => setIsLinkedReferencesOpen(!isLinkedReferencesOpen);
  const handlePressUnlinkedReferencesToggle = () => setIsUnlinkedReferencesOpen(!isUnlinkedReferencesOpen);
  return <Page
    uid="123"
    isLinkedReferencesOpen={isLinkedReferencesOpen}
    isUnlinkedReferencesOpen={isUnlinkedReferencesOpen}
    handlePressLinkedReferencesToggle={handlePressLinkedReferencesToggle}
    handlePressUnlinkedReferencesToggle={handlePressUnlinkedReferencesToggle}
    {...args}
  />
};

export const NodePage = Template.bind({});
NodePage.args = {
  title: "Node Page",
  isDailyNote: false,
  children: <Page.BlocksContainer><BlockTree /></Page.BlocksContainer>,
}

export const BlockPage = Template.bind({});
BlockPage.args = {
  isDailyNote: false,
  title: 'Block Page',
  children: <Page.BlocksContainer><BlockTree /></Page.BlocksContainer>
};

export const PageWithPresence = Template.bind({});
PageWithPresence.args = {
  isDailyNote: false,
  title: 'Block Page',
  children: <Page.BlocksContainer><WithPresence /></Page.BlocksContainer>
};

export const NodePageWithTasks = Template.bind({});
NodePageWithTasks.args = {
  isDailyNote: false,
  title: 'Node Page With Tasks',
  children: <Page.BlocksContainer><WithTasks /></Page.BlocksContainer>
};

export const BlockPageWithLongTitle = Template.bind({});
BlockPageWithLongTitle.args = {
  isDailyNote: false,
  title: 'Lorem ipsum dolor sit amet donec consectetur',
  children: <Page.BlocksContainer><BlockTree /></Page.BlocksContainer>
};

export const DailyNote = Template.bind({});
DailyNote.args = {
  isDailyNote: true,
  hasShortcut: false,
  children: <Page.BlocksContainer><BlockTree /></Page.BlocksContainer>,
  title: DateTime.now().toLocaleString(preferredDateFormat),
  uid: DateTime.now().toISODate()
};
