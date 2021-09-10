import styled from 'styled-components'
import React from 'react';

import { DOMRoot } from '../../config';
import {
  Wifi, SettingsEthernet, SyncProblem, Lock,
} from '@material-ui/icons'
import { Popper, Modal, Fade, PopperPlacementType } from '@material-ui/core'

import { Button } from '../Button';
import { Menu } from '../Menu';
import { Overlay } from '../Overlay';
import { Avatar } from '../Avatar';
import { ProfileSettingsDialog } from '../ProfileSettingsDialog';


const ConnectionButton = styled(Button)`
  gap: 0.125rem;
`;

const MemberCount = styled.span`
  margin-left: 0.125rem;
`;

const Heading = styled.h3`
  margin: 0;
  font-weight: normal;
  padding: 0.25rem 0.5rem;
  font-size: var(--font-size--text-xs);
  color: var(--body-text-color---opacity-med);
`;

const PresenceOverlay = styled(Overlay)`
  min-width: 14em;
`;

const HostIcon = styled(Wifi)`
  padding: 0.25rem;
  background: var(--background-minus-2);
  border-radius: 100em;
`;

const PersonWrap = styled.div`
  padding: 0.375rem 0.5rem;
  display: flex;
  align-items: center;
`;

const Profile = styled.div`
  display: flex;
  align-items: center;
  flex-wrap: wrap;

  ${Heading} {
    flex: 1 1 100%;
    font-size: var(--font-size--text-sm);
  }

  ${Avatar.Wrapper} {
    margin-right: 0.5rem;
  }

  button {
    margin-left: auto;
    font-size: var(--font-size--text-xs);
  }
`;

const ConnectionArea = styled.div`
  display: grid;
  grid-template-areas: 'icon status' 'icon detail';
  align-items: center;
  gap: 0 0.25rem;
  line-height: 1.2;
  width: max-content;

  &.connecting {
    color: var(--link-color);
  }

  &.reconnecting {
    color: var(--highlight-color);
  }

  &.offline {
    svg {
      color: var(--body-text-color);
    }
  }

  svg {
    font-size: 1em;
    grid-area: icon;
  }

  span {
    grid-area: status;
    font-size: var(--font-size--text-sm);
  }

  span + span {
    font-size: var(--font-size--text-xs);
    grid-area: detail;
  }
`;

interface ConnectionStatusIndicatorProps {
  status: ConnectionStatus;
}

const ConnectionStatusIndicator = ({
  status
}: ConnectionStatusIndicatorProps) => {
  switch (status) {
    case 'connecting':
      return <><SettingsEthernet /><span>Connecting...</span></>;
    case 'reconnecting':
      return <><SyncProblem /><span>Reconnecting...</span></>;
    case 'offline':
      return <><Lock /><span>View Only</span></>;
    case 'connected':
    default:
      return null
  }
}

export interface PresenceDetailsProps {
  hostAddress: HostAddress
  currentUser: Person
  placement?: PopperPlacementType
  currentPageMembers: Person[]
  differentPageMembers: Person[]
  handleUpdateProfile(person: Person): void
  handlePressHostAddress(hostAddress: HostAddress): void
  handlePressMember(person: Person): void,
  connectionStatus: ConnectionStatus
}

export const PresenceDetails = ({
  hostAddress,
  currentUser,
  placement,
  currentPageMembers,
  differentPageMembers,
  handlePressHostAddress,
  handlePressMember,
  handleUpdateProfile,
  connectionStatus,
}: PresenceDetailsProps) => {
  const maxToDisplay = 5;
  const [isPresenceDetailsOpen, setIsPresenceDetailsOpen] = React.useState<boolean>(false);
  const [presenceDetailsAnchor, setPresenceDetailsAnchor] = React.useState<HTMLButtonElement | null>(null);
  const [isUserSettingsDialogOpen, setIsUserSettingsDialogOpen] = React.useState<boolean>(false);
  const [timeLastOnline, setTimeLastOnline] = React.useState<string | null>(null);

  const showablePersons = [...currentPageMembers, ...differentPageMembers];

  React.useEffect(() => {
    if (connectionStatus === 'offline') {
      const currentTime = new Date().toLocaleTimeString();
      setTimeLastOnline(currentTime);
    } else {
      setTimeLastOnline(null);
    }
  }, [connectionStatus]);

  return (
    <>
      <ConnectionArea className={connectionStatus}>
        <ConnectionStatusIndicator status={connectionStatus} />
        <span>{timeLastOnline && timeLastOnline}</span>
      </ConnectionArea>

      <ConnectionButton
        ref={setPresenceDetailsAnchor}
        onClick={() => setIsPresenceDetailsOpen(!isPresenceDetailsOpen)}
        shape="round"
        isPressed={isPresenceDetailsOpen}>

        {connectionStatus === 'connected' && (
          <>
            <Avatar.Stack
              size="1em"
              maskSize="1.5px"
              overlap="0.2"
            >
              {showablePersons && showablePersons.slice(0, maxToDisplay).map((member, index) => {
                if (index < maxToDisplay) {
                  return (
                    <Avatar
                      key={member.personId}
                      username={member.username}
                      color={member.color}
                      personId={member.personId}
                      showTooltip={false}
                    />
                  );
                }
                return null;
              })}
            </Avatar.Stack>
            {showablePersons.length > maxToDisplay && <MemberCount>+{showablePersons.length - maxToDisplay}</MemberCount>}
          </>
        )}
      </ConnectionButton>

      {connectionStatus === 'connected' && (
        <Modal
          container={DOMRoot}
          open={isPresenceDetailsOpen}
          BackdropProps={{ invisible: true }}
          onClose={() => setIsPresenceDetailsOpen(false)}
        >
          <Popper
            open={isPresenceDetailsOpen}
            placement={placement ? placement : 'bottom-end'}
            disablePortal={true}
            anchorEl={presenceDetailsAnchor}
            transition>
            {({ TransitionProps }) => (
              <Fade {...TransitionProps} timeout={250}>
                <PresenceOverlay className="animate-in">
                  {hostAddress && (<>
                    <Button onClick={(hostAddress) => handlePressHostAddress(hostAddress)}>
                      <HostIcon />
                      <span>{hostAddress}</span>
                    </Button>
                    <Menu.Separator />
                  </>)}
                  <Profile>
                    <Heading>You appear as</Heading>
                    <PersonWrap>
                      <Avatar
                        username={currentUser.username}
                        personId={currentUser.personId}
                        color={currentUser.color}
                        showTooltip={false}
                      />
                      <span>{currentUser.username}</span>
                    </PersonWrap>
                    <Button
                      onClick={() => setIsUserSettingsDialogOpen(true)}
                      key={currentUser.personId}
                    >
                      Edit
                    </Button>
                  </Profile>
                  <Menu.Separator />

                  {currentPageMembers.length > 0 && (
                    <>
                      <Heading>On this page</Heading>
                      <Menu>
                        {currentPageMembers.map(member =>
                          <Button onClick={(member) => handlePressMember(member)} key={member.personId}>
                            <Avatar
                              username={member.username}
                              personId={member.personId}
                              color={member.color}
                              showTooltip={false}
                            />
                            <span>{member.username}</span>
                          </Button>
                        )}
                      </Menu>
                      <Menu.Separator />
                    </>
                  )}

                  <Menu>
                    {differentPageMembers.length > 0 && differentPageMembers.map(member =>
                      <Button onClick={(member) => handlePressMember(member)} key={member.personId}>
                        <Avatar
                          username={member.username}
                          personId={member.personId}
                          color={member.color}
                          showTooltip={false}
                          isMuted={true}
                        />
                        <span>{member.username}</span>
                      </Button>
                    )}
                  </Menu>
                </PresenceOverlay>
              </Fade>
            )}
          </Popper>
        </Modal>
      )}

      {isUserSettingsDialogOpen &&
        <ProfileSettingsDialog
          person={{ ...currentUser }}
          isOpen={isUserSettingsDialogOpen}
          handleClose={() => setIsUserSettingsDialogOpen(false)}
          handleUpdatePerson={(person) => {
            handleUpdateProfile(person)
            setIsUserSettingsDialogOpen(false)
          }}
        />}
    </>
  );
};


