import styled from 'styled-components';

import { Page } from '../Page';
import { BADGE, Storybook } from '../../storybook';
import { DateTime } from 'luxon';
import { preferredDateFormat } from '../../config';

import { BlockTree, BlockTreeWithTasks } from '../Block/Block.stories';
import { PageBlocksContainer } from './Page';

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

const Template = (args) => <Page {...args} />;

export const NodePage = () => <Page
  handlePressAddShortcut={() => null}
  handlePressDelete={() => null}
  handlePressRemoveShortcut={() => null}
  handlePressShowLocalGraph={() => null}
  title="Node Page"
  isDailyNote={false}
  children={<PageBlocksContainer><BlockTree /></PageBlocksContainer>}
  uid="123" />;

export const BlockPage = Template.bind({});
BlockPage.args = {
  isDailyNote: false,
  title: 'Block Page',
  uid: '123',
  children: <PageBlocksContainer><BlockTree /></PageBlocksContainer>
};

export const NodePageWithTasks = Template.bind({});
BlockPage.args = {
  isDailyNote: false,
  title: 'Node Page With Tasks',
  uid: '123',
  children: <PageBlocksContainer><BlockTreeWithTasks /></PageBlocksContainer>
};

export const BlockPageWithLongTitle = Template.bind({});
BlockPageWithLongTitle.args = {
  isDailyNote: false,
  title: 'Lorem ipsum dolor sit amet donec consectetur',
  uid: '123',
  children: <PageBlocksContainer><BlockTree /></PageBlocksContainer>
};

export const DailyNote = Template.bind({});
DailyNote.args = {
  isDailyNote: true,
  hasShortcut: false,
  children: <PageBlocksContainer><BlockTree /></PageBlocksContainer>,
  title: DateTime.now().toLocaleString(preferredDateFormat),
  uid: DateTime.now().toISODate()
};
