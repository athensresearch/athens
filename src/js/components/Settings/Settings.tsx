import styled from 'styled-components';
import { Modal } from '@material-ui/core';
import { Close, Block, CheckBox } from '@material-ui/icons';

import { DOMRoot } from '../../config';

import { Input } from '../Input';
import { Overlay } from '../Overlay';
import { Button } from '../Button';

import { OpenCollectiveSettings } from './components/OpenCollectiveSettings';

const SettingsWrap = styled(Overlay)`
  width: 100%;
  max-width: 900px;
  display: flex;
  margin: 4rem auto;
  padding: 0 2rem;
`;

const SettingsHeader = styled.header`
  display: flex;
  justify-content: space-between;
  align-items: center;
`;

export const Settings = ({
  openCollectiveEmail = "jeff@athens.org",
  isUpdatingEmail = false,
  handleUpdateEmail,
  handleCloseSettings,
}) => {
  return (
    <Modal
      onClose={handleCloseSettings}
      container={DOMRoot}
      open={true}
    >
      <SettingsWrap className="animate-in">
        <SettingsHeader>
          <h1>Settings</h1>
          <Button onClick={handleCloseSettings}><Close /></Button>
        </SettingsHeader>

        <OpenCollectiveSettings
          openCollectiveEmail={openCollectiveEmail}
          handleUpdateEmail={handleUpdateEmail}
          isUpdatingEmail={isUpdatingEmail}
        />
        {/*
        <Setting
          label="Usage and Diagnostics"
        // glance={openCollectiveEmail ? (<><CheckBox /> {openCollectiveEmail}</>) : (<><Block /> Not set</>)}
        // body={<>
        //   <input
        //     type="email"
        //     placeholder="OpenCollective Email"
        //     onChange={handleChangeEmail}
        //     defaultValue={openCollectiveEmail}
        //   />
        //   <Button
        //     isPrimary={true}
        //     onClick={handleSubmitEmail}
        //   >Submit</Button>
        //   <Button
        //     onClick={handleResetEmail}
        //   >Reset</Button>
        // </>}
        // help={<p>{openCollectiveEmail !== '' ? "You are using the free version of Athens. You are hosting your own data. Please be careful!" : "Thank you for supporting Athens! Backups are coming soon."}</p>}
        />

        <Setting
          label="Backups"
        // glance={}
        // body={<>
        //   <input
        //     type="email"
        //     placeholder="OpenCollective Email"
        //     onChange={handleChangeEmail}
        //     defaultValue={openCollectiveEmail}
        //   />
        //   <Button
        //     isPrimary={true}
        //     onClick={handleSubmitEmail}
        //   >Submit</Button>
        //   <Button
        //     onClick={handleResetEmail}
        //   >Reset</Button>
        // </>}
        // help={<p>{openCollectiveEmail !== '' ? "You are using the free version of Athens. You are hosting your own data. Please be careful!" : "Thank you for supporting Athens! Backups are coming soon."}</p>}
        />

        <Setting
          label="Remote Backup"
        // glance={}
        // body={<>
        //   <input
        //     type="email"
        //     placeholder="OpenCollective Email"
        //     onChange={handleChangeEmail}
        //     defaultValue={openCollectiveEmail}
        //   />
        //   <Button
        //     isPrimary={true}
        //     onClick={handleSubmitEmail}
        //   >Submit</Button>
        //   <Button
        //     onClick={handleResetEmail}
        //   >Reset</Button>
        // </>}
        // help={<p>{openCollectiveEmail !== '' ? "You are using the free version of Athens. You are hosting your own data. Please be careful!" : "Thank you for supporting Athens! Backups are coming soon."}</p>}
        />

        <Setting
          label="Multiplayer"
        // glance={}
        // body={<>
        //   <input
        //     type="email"
        //     placeholder="OpenCollective Email"
        //     onChange={handleChangeEmail}
        //     defaultValue={openCollectiveEmail}
        //   />
        //   <Button
        //     isPrimary={true}
        //     onClick={handleSubmitEmail}
        //   >Submit</Button>
        //   <Button
        //     onClick={handleResetEmail}
        //   >Reset</Button>
        // </>}
        // help={<p>{openCollectiveEmail !== '' ? "You are using the free version of Athens. You are hosting your own data. Please be careful!" : "Thank you for supporting Athens! Backups are coming soon."}</p>}
        />
 */}

      </SettingsWrap>
    </Modal>
  )
};