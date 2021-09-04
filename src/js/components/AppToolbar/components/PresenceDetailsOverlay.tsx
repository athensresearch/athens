import styled from 'styled-components'

import { DOMRoot } from '../../../config';
import { Wifi } from '@material-ui/icons'
import { Popper, Modal, Fade } from '@material-ui/core'

import { Button } from '../../Button';
import { Menu } from '../../Menu';
import { Overlay } from '../../Overlay';
import { Avatar } from '../../Avatar';

const Heading = styled.h3`
  margin: 0;
  padding: 0.25rem 0.5rem;
  font-size: var(--font-size--text-xs);
  color: var(--body-text-color---opacity-med);
`;

const PresenceOverlay = styled(Overlay)`
  min-width: 12em;
`;

interface PresenceDetailsProps {
  hostAddress: string,
  currentPageMembers: PersonPresence[]
  differentPageMembers: PersonPresence[],
  presenceDetailsAnchor: HTMLElement | null,
  isPresenceDetailsOpen: boolean,
  handlePressMember(member): void,
  handlePressHostAddress(): void,
  setIsPresenceDetailsOpen(boolean): void
}

export const PresenceDetailsOverlay = ({
  hostAddress,
  currentPageMembers,
  differentPageMembers,
  handlePressHostAddress,
  setIsPresenceDetailsOpen,
  handlePressMember,
  isPresenceDetailsOpen,
  presenceDetailsAnchor
}: PresenceDetailsProps) => {
  return (
    <Modal
      container={DOMRoot}
      open={isPresenceDetailsOpen}
      BackdropProps={{ invisible: true }}
      onClose={() => setIsPresenceDetailsOpen(false)}
    >
      <Popper
        open={isPresenceDetailsOpen}
        placement="bottom-end"
        disablePortal={true}
        anchorEl={presenceDetailsAnchor}
        transition>
        {({ TransitionProps }) => (
          <Fade {...TransitionProps} timeout={250}>
            <PresenceOverlay>
              {hostAddress && <><Button onClick={handlePressHostAddress}><Wifi /> <span>{hostAddress}</span></Button>
                <Menu.Separator /></>
              }
              {currentPageMembers.length > 0 && (
                <>
                  <Heading>On this page</Heading>
                  <Menu>
                    {currentPageMembers.length > 0 && currentPageMembers.map(member =>
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
  )
}
