import styled from 'styled-components';
import { Modal } from '@material-ui/core';
import { Close, Block, CheckBox } from '@material-ui/icons';

import { Overlay } from '../Overlay';
import { Button } from '../Button';

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

const SettingWrap = styled.div`
  border-top: 1px solid var(--border-color);
  padding: 2rem 0.75rem;
  line-height: 1.25;

  h3 {
    margin: 0;
  }

  &.disabled {
    opacity: 0.5;
  }

  header {
    padding-bottom: 1rem;
  }

  .glance {
    font-weight: normal;
    opacity: var(--opacity-high);
    font-size: 0.8em;
    gap: 0.25em;

    svg {
      vertical-align: -0.25rem;
      font-size: 1.5em;
    }
  }

  aside {
    font-size: 0.8em;
    padding-top: 0.5rem;

    p {
      margin: 0.25rem 0;

      &:first-child {
        margin-top: 0;
      }
      &:last-child {
        margin-bottom: 0;
      }
    }
  }

  label {
    display: flex;
    align-items: center;
    font-weight: bold;
    gap: 0.5rem;
  }

  @media all and (min-width: 40em) {
    display: grid;
    grid-template-columns: 10rem 1fr;
    grid-gap: 1rem;
  }
`;

const Setting = ({ label, glance, body, help }) =>
  <SettingWrap>
    <header>
      <h3>{label}</h3>
      {glance && <span className="glance">{glance}</span>}
    </header>
    <main>
      <div>
        {body}
      </div>
      {help && <aside>
        {help}
      </aside>}
    </main>
  </SettingWrap>;


export const Settings = ({
  openCollectiveEmail,
  handleResetEmail,
  handleChangeEmail,
  handleSubmitEmail,
  handleCloseSettings,
}) => {
  return (
    <Modal
      onClose={handleCloseSettings}
      // BackdropProps={{ invisible: true }}
      container={() => document.querySelector('#app')}
      open={true}
    >
      <SettingsWrap className="animate-in">
        <SettingsHeader>
          <h1>Settings</h1>
          <Button onClick={handleCloseSettings}><Close /></Button>
        </SettingsHeader>

        <Setting
          label="Email"
          glance={openCollectiveEmail ? (<><CheckBox /> {openCollectiveEmail}</>) : (<><Block /> Not set</>)}
          body={<>
            <input
              type="email"
              placeholder="OpenCollective Email"
              onChange={handleChangeEmail}
              defaultValue={openCollectiveEmail}
            />
            <Button
              isPrimary={true}
              onClick={handleSubmitEmail}
            >Submit</Button>
            <Button
              onClick={handleResetEmail}
            >Reset</Button>
          </>}
          help={<p>{openCollectiveEmail !== '' ? "You are using the free version of Athens. You are hosting your own data. Please be careful!" : "Thank you for supporting Athens! Backups are coming soon."}</p>}
        />

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


      </SettingsWrap>
    </Modal>
  )
};