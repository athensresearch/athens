import React from 'react';

import { BADGE, Storybook } from '../../storybook';
import { ProfileSettingsDialog } from './ProfileSettingsDialog';


import { Avatar } from '../Avatar';
import { Button } from '../Button';

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
  const [isDialogOpen, setIsDialogOpen] = React.useState(args.isOpen);
  const [person, setPerson] = React.useState(testPerson);

  const handleUpdatePerson = React.useCallback((person) => {
    setPerson(person);
    setIsDialogOpen(false);
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
          onClick={() => setIsDialogOpen(true)}>
          Edit
        </Button>
      </div>
      <Storybook.Wrapper>
        <ProfileSettingsDialog
          person={testPerson}
          isOpen={isDialogOpen}
          handleUpdatePerson={handleUpdatePerson}
          handleClose={() => setIsDialogOpen(false)}
        />
      </Storybook.Wrapper>
    </>)
};

export const Default = Template.bind({});

export const Shown = Template.bind({});
Shown.args = {
  isOpen: true
}
