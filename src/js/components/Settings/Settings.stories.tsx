import React from 'react';
import { BADGE, Storybook } from '../../storybook';

import { Settings } from './Settings';
import { Button } from '../Button';

export default {
  title: 'Routes/Settings',
  component: Settings,
  argTypes: {},
  parameters: {
    badges: [BADGE.DEV]
  },
};

export const Basic = () => {
  const [isSettingsOpen, setIsSettingsOpen] = React.useState(true);
  const [openCollectiveEmail, setOpenCollectiveEmail] = React.useState(null);
  const [openCollectiveEmailSubmitted, setOpenCollectiveEmailSubmitted] = React.useState(!!openCollectiveEmail);

  const handleResetEmail = () => setOpenCollectiveEmail(null);
  const handleChangeEmail = (e) => setOpenCollectiveEmail(e.target.value);
  const handleSubmitEmail = () => setOpenCollectiveEmailSubmitted(true);

  return (
    isSettingsOpen ? <Settings
      openCollectiveEmail={openCollectiveEmail}
      handleResetEmail={handleResetEmail}
      handleChangeEmail={handleChangeEmail}
      handleSubmitEmail={handleSubmitEmail}
      handleCloseSettings={() => setIsSettingsOpen(false)}
    /> : <Button onClick={() => setIsSettingsOpen(true)}>Open Settings</Button>
  );
};