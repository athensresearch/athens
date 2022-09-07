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
  decorators: [(Story) => <Storybook.Wrapper style={{
    display: 'flex',
    justifyContent: 'center',
    alignItems:
      'center'
  }}><Story /></Storybook.Wrapper>]
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

export const ConnectedButAlone = Template.bind({});
ConnectedButAlone.args = {
  currentPageMembers: [],
  differentPageMembers: [],
}

export const Open = Template.bind({});
Open.args = { defaultOpen: true }

export const ConnectionStatus = (args) => {
  const {
    currentUser,
    setCurrentUser,
    currentPageMembers,
    differentPageMembers,
    hostAddress,
    connectionStatus,
  } = useAppState();

  return (<>
    <PresenceDetails
      hostAddress={hostAddress}
      currentPageMembers={currentPageMembers}
      differentPageMembers={differentPageMembers}
      currentUser={currentUser}
      handleUpdateProfile={(person) => setCurrentUser(person)}
      {...args}
      connectionStatus={'local'}
    />
    <PresenceDetails
      hostAddress={hostAddress}
      currentPageMembers={currentPageMembers}
      differentPageMembers={differentPageMembers}
      currentUser={currentUser}
      handleUpdateProfile={(person) => setCurrentUser(person)}
      {...args}
      connectionStatus={'connecting'}
    />
    <PresenceDetails
      hostAddress={hostAddress}
      currentPageMembers={currentPageMembers}
      differentPageMembers={differentPageMembers}
      currentUser={currentUser}
      handleUpdateProfile={(person) => setCurrentUser(person)}
      {...args}
      connectionStatus={'connected'}
    />
    <PresenceDetails
      hostAddress={hostAddress}
      currentPageMembers={currentPageMembers}
      differentPageMembers={differentPageMembers}
      currentUser={currentUser}
      handleUpdateProfile={(person) => setCurrentUser(person)}
      {...args}
      connectionStatus={'reconnecting'}
    />
    <PresenceDetails
      hostAddress={hostAddress}
      currentPageMembers={currentPageMembers}
      differentPageMembers={differentPageMembers}
      currentUser={currentUser}
      handleUpdateProfile={(person) => setCurrentUser(person)}
      {...args}
      connectionStatus={'offline'}
    />
  </>)
};
