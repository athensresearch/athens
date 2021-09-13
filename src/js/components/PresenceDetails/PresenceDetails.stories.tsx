import { BADGE, Storybook } from '@/utils/storybook';

import { useAppState } from '@/utils/useAppState';

import { PresenceDetails } from './PresenceDetails';

export default {
  title: 'Components/PresenceDetails',
  component: PresenceDetails,
  argTypes: {
    connectionStatus: {
      options: ['local', 'connecting', 'connected', 'reconnecting', 'offline'],
      control: { type: 'radio' },
      defaultValue: 'connected'
    }
  },
  parameters: {
    badges: [BADGE.DEV, BADGE.IN_USE]
  },
  decorators: [(Story) => {


    return <Storybook.Wrapper><Story /></Storybook.Wrapper>
  }]
};

const Template = (args) => {
  const {
    currentUser,
    setCurrentUser,
    currentPageMembers,
    differentPageMembers,
    hostAddress,
    connectionStatus,
  } = useAppState();

  return (<PresenceDetails
    hostAddress={hostAddress}
    currentPageMembers={currentPageMembers}
    differentPageMembers={differentPageMembers}
    connectionStatus={args.connectionStatus || connectionStatus}
    currentUser={currentUser}
    handleUpdateProfile={(person) => setCurrentUser(person)}
    placement="bottom-start"
    {...args} />)
};

export const Default = Template.bind({});
Default.args = {}
