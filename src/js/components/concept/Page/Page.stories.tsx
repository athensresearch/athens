import React from 'react';

import { Page } from '.';
import { BADGE, Storybook } from '../../../storybook';
import { DateTime } from 'luxon';
import { preferredDateFormat } from '../../../config';

import {
  WithToggle,
  WithSelection,
  WithChecklist,
  WithPresence
} from '../Block/Block.stories';

export default {
  title: 'Concepts/Page',
  component: Page,
  argTypes: {},
  parameters: {
    badges: [BADGE.DEV]
  },
  decorators: [(Story) => <Storybook.Wrapper><Story /></Storybook.Wrapper>]
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
  children: <Page.BlocksContainer><WithToggle /></Page.BlocksContainer>,
}

export const BlockPage = Template.bind({});
BlockPage.args = {
  isDailyNote: false,
  title: 'Block Page',
  children: <Page.BlocksContainer><WithToggle /></Page.BlocksContainer>
};

export const PageWithPresence = Template.bind({});
PageWithPresence.args = {
  isDailyNote: false,
  title: 'Block Page',
  children: <Page.BlocksContainer><WithPresence /></Page.BlocksContainer>
};

export const PageWithChecklist = Template.bind({});
PageWithChecklist.args = {
  isDailyNote: false,
  title: 'Node Page With Tasks',
  children: <Page.BlocksContainer><WithChecklist /></Page.BlocksContainer>
};

export const PageWithSelection = Template.bind({});
PageWithSelection.args = {
  isDailyNote: false,
  title: 'Node Page With Selection',
  children: <Page.BlocksContainer><WithSelection /></Page.BlocksContainer>
};

export const PageWithLongTitle = Template.bind({});
PageWithLongTitle.args = {
  isDailyNote: false,
  title: 'Lorem ipsum dolor sit amet donec consectetur',
  children: <Page.BlocksContainer><WithToggle /></Page.BlocksContainer>
};

export const DailyNote = Template.bind({});
DailyNote.args = {
  isDailyNote: true,
  hasShortcut: false,
  children: <Page.BlocksContainer><WithToggle /></Page.BlocksContainer>,
  title: DateTime.now().toLocaleString(preferredDateFormat),
  uid: DateTime.now().toISODate()
};
