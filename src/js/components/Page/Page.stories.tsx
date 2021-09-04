import { Page } from '../Page';
import { BADGE } from '../../storybook';
import { DateTime } from 'luxon';
import { Welcome } from '../Block/Block.stories';
import { preferredDateFormat } from '../../config';

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
      handlePressAddShortcut={() => null}
      handlePressRemoveShortcut={() => null}
      handlePressDelete={() => null}
      handlePressShowLocalGraph={() => null}
    >
      <Welcome />
    </Page>
  );
}