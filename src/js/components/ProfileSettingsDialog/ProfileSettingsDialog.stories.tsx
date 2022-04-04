import React from 'react';

import { useOverlayTriggerState } from "@react-stately/overlays";

import { BADGE, Storybook } from '@/utils/storybook';
import { ProfileSettingsDialog } from './ProfileSettingsDialog';

import { Avatar } from '../Avatar';
import { Button } from '@/Button';

export default {
  title: 'Components/ProfileSettingsDialog',
  component: ProfileSettingsDialog,
  argTypes: {},
  parameters: {
    layout: 'centered',
    badges: [BADGE.DEV, BADGE.IN_USE]
  }
};

const testPerson = { personId: '123', username: 'John Doe', color: '#0071ed' };

const Template = (args, context) => {
  let profileSettingsState = useOverlayTriggerState({
    defaultOpen: args.defaultOpen,
    onOpenChange: (open) => {
      console.log('ProfileSettingsDialog open state changed to: ', open);
    },
  });
  const [person, setPerson] = React.useState(testPerson);

  const handleUpdatePerson = React.useCallback((person) => {
    setPerson(person);
    profileSettingsState.close();
  }, []);

  return (
    <>
      <div style={{ display: "flex", flexDirection: 'column', gap: "3rem", alignItems: 'center' }}>
        <Avatar
          size="3em"
          {...person}
          showTooltip={true}
          tooltipPlacement="bottom"
        />
        <Button
          shape="round"
          variant="gray"
          onClick={profileSettingsState.open}>
          Edit
        </Button>
      </div>
      <Storybook.Wrapper>
        <ProfileSettingsDialog
          person={testPerson}
          isOpen={profileSettingsState.isOpen}
          onClose={profileSettingsState.close}
          onUpdatePerson={handleUpdatePerson}
        />
      </Storybook.Wrapper>
    </>)
};

export const Default = Template.bind({});
Default.args = {
  defaultOpen: true
}
