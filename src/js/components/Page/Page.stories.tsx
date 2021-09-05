import { Page } from '../Page';
import { BADGE, Storybook } from '../../storybook';
import { DateTime } from 'luxon';
import { preferredDateFormat } from '../../config';

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
  uid="123" />;

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
