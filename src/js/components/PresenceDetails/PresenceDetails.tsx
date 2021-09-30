import styled, { keyframes } from "styled-components";
import React from "react";

import { RefreshDouble, Lock } from "iconoir-react";

import { mergeProps } from "@react-aria/utils";
import {
  useOverlay,
  useOverlayTrigger,
  useOverlayPosition,
  useModal,
  OverlayContainer,
} from "@react-aria/overlays";
import { useOverlayTriggerState } from "@react-stately/overlays";
import { FocusScope } from "@react-aria/focus";
import { useDialog } from "@react-aria/dialog";

import { Button } from "@/Button";
import { Menu } from "@/Menu";
import { Overlay } from "@/Overlay";
import { Backdrop } from "@/Overlay/Backdrop";
import { Avatar } from "@/Avatar";
import { ProfileSettingsDialog } from "@/ProfileSettingsDialog";
import { ConnectedGraphConnection } from "@/Icons/ConnectedGraphConnection";
import { ConnectedGraphHost } from "@/Icons/ConnectedGraphHost";
import { Icon } from "@/Icons/Icon";

const ConnectionButton = styled(Button)`
  gap: 0.125rem;
  transition: all 0s;
  min-height: 2rem;
  font-size: var(--font-size--text-xs);
  border: 1px solid var(--border-color);

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
`;

const PresenceOverlay = styled(Overlay)`
  min-width: 8rem;
  flex-direction: column;
  max-height: calc(100vh - 4rem);
  overflow-y: auto;
`;

const HostIconWrap = styled(Icon)`
  --size: 2rem;
  border-radius: 18%;
  background: var(--background-minus-2);
  padding: 0.25rem;
`;

const HostIcon = () => (
  <HostIconWrap>
    <ConnectedGraphHost />
  </HostIconWrap>
);

const PersonWrap = styled.div`
  padding: 0.375rem 0.5rem;
  display: flex;
  align-items: center;
`;

const Profile = styled.div`
  display: flex;
  align-items: center;
  flex-wrap: wrap;

  svg {
    margin-right: 0.5rem;
  }

  button {
    margin-left: auto;
    font-size: var(--font-size--text-xs);
  }
`;

const Host = styled(Profile)`
  padding-top: 0.25rem;
  padding-bottom: 0.25rem;

  svg {
    margin-left: 0.25rem;
  }
`;

const rotate = keyframes`
  from {
    transform: rotate(0deg);
  } to {
    transform: rotate(360deg);
  }
`;

const OfflineIcon = styled(Lock)`
  width: 1.25rem;
  height: 1.25rem;
`;

const ActivityIcon = styled(RefreshDouble)`
  width: 1.25rem;
  height: 1.25rem;
  animation: ${rotate} 2s linear infinite;
  stroke-width: 2;
  vector-effect: non-scaling-stroke;
`;

const connectionStatusIndicator = {
  connected: (
    <Icon style={{ "--size": "1.5rem" }}>
      <ConnectedGraphConnection />
    </Icon>
  ),
  connecting: (
    <>
      <ActivityIcon />
      <span>Connecting...</span>
    </>
  ),
  reconnecting: (
    <>
      <ActivityIcon />
      <span>Reconnecting...</span>
    </>
  ),
  offline: (
    <>
      <OfflineIcon />
      <span>View Only</span>
    </>
  ),
};

const connectionStatusHelpText = {
  connecting: "Athens is connecting to the host.",
  connected: "View connection details.",
  reconnecting: "Athens is attempting to reconnect to the host.",
  offline: "Athens is not connected.",
};

export interface PresenceDetailsProps {
  hostAddress: HostAddress;
  currentUser: Person;
  currentPageMembers: Person[];
  differentPageMembers: Person[];
  handleUpdateProfile(person: Person): void;
  handleCopyHostAddress(hostAddress: HostAddress): void;
  handlePressMember(person: Person): void;
  connectionStatus: ConnectionStatus;
  defaultOpen?: boolean;
}

interface PresenceDetailsPopoverProps {
  children: React.ReactNode;
  isOpen: boolean;
  onClose: () => void;
}

const PresenceDetailsPopover = React.forwardRef(
  (
    { isOpen, onClose, children, ...otherProps }: PresenceDetailsPopoverProps,
    ref: RefObject<HTMLElement>
  ) => {
    let { overlayProps, underlayProps } = useOverlay(
      {
        onClose,
        isOpen,
        isDismissable: true,
      },
      ref
    );

    let { modalProps } = useModal();
    let { dialogProps, titleProps } = useDialog({}, ref);

    return (
      <OverlayContainer>
        <Backdrop hidden={true} {...underlayProps}>
          <FocusScope contain restoreFocus autoFocus>
            <PresenceOverlay
              {...mergeProps(overlayProps, dialogProps, otherProps, modalProps)}
              ref={ref}
              className="animate-in"
            >
              <Menu.Heading {...titleProps}>Connection & Presence</Menu.Heading>
              {children}
            </PresenceOverlay>
          </FocusScope>
        </Backdrop>
      </OverlayContainer>
    );
  }
);

export const PresenceDetails = (props: PresenceDetailsProps) => {
  const {
    hostAddress,
    currentUser,
    currentPageMembers,
    differentPageMembers,
    handleCopyHostAddress,
    handlePressMember,
    handleUpdateProfile,
    connectionStatus,
    defaultOpen,
  } = props;
  const showablePersons = [...currentPageMembers, ...differentPageMembers];

  // State and controllers for the menu
  let menuState = useOverlayTriggerState({ defaultOpen: defaultOpen });

  let triggerRef = React.useRef();
  let overlayRef = React.useRef();

  let { triggerProps: presenceMenuTriggerProps, overlayProps: presenceMenuOverlayProps } = useOverlayTrigger(
    { type: "listbox" },
    menuState,
    triggerRef,
  );

  let { overlayProps: positionProps } = useOverlayPosition({
    targetRef: triggerRef,
    overlayRef,
    placement: "bottom end",
    offset: 2,
    isOpen: menuState.isOpen,
  });

  // State and controllers for the profile settings dialog
  let profileSettingsState = useOverlayTriggerState({});

  return connectionStatus === "local" ? (
    <></>
  ) : (
    <>
      <ConnectionButton
        ref={triggerRef}
        disabled={connectionStatus !== "connected"}
        onClick={menuState.open}
        shape="round"
        className={connectionStatus}
        title={connectionStatusHelpText[connectionStatus]}
        isPressed={menuState.isOpen}
        {...presenceMenuTriggerProps}
      >
        {connectionStatusIndicator[connectionStatus]}

        {connectionStatus === "connected" && showablePersons.length > 0 && (
          <>
            <Avatar
              key={currentUser.personId}
              username={currentUser.username}
              color={currentUser.color}
              personId={currentUser.personId}
              showTooltip={false}
              isOutlined={true}
              size="1.25rem"
            />
            <Avatar.Stack
              size="1.25rem"
              maskSize="1.5px"
              overlap={0.2}
              limit={5}
            >
              {showablePersons.map((member) => (
                <Avatar key={member.personId} {...member} showTooltip={false} />
              ))}
            </Avatar.Stack>
          </>
        )}
      </ConnectionButton>

      {menuState.isOpen && (
        <OverlayContainer>
          <PresenceDetailsPopover
            {...presenceMenuOverlayProps}
            {...positionProps}
            ref={overlayRef}
            isOpen={menuState.isOpen}
            onClose={menuState.close}
          >
            <>
              {hostAddress && (
                <>
                  <Host>
                    <HostIcon />
                    <span>{hostAddress}</span>
                    <Button
                      onClick={() => handleCopyHostAddress(hostAddress)}
                    >Copy
                    </Button>
                  </Host>
                  <Menu.Separator />
                </>
              )}
              <Profile>
                <Menu.Heading>You appear as</Menu.Heading>
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
                  onClick={profileSettingsState.open}
                  key={currentUser.personId}
                >
                  Edit
                </Button>
              </Profile>

              {currentPageMembers.length > 0 && (
                <>
                  <Menu.Separator />
                  <Menu.Heading>On this page</Menu.Heading>
                  <Menu>
                    {currentPageMembers.map((member) => (
                      <Button
                        onClick={() => handlePressMember(member)}
                        key={member.personId}
                      >
                        <Avatar
                          username={member.username}
                          personId={member.personId}
                          color={member.color}
                          showTooltip={false}
                        />
                        <span>{member.username}</span>
                      </Button>
                    ))}
                  </Menu>
                </>
              )}

              {differentPageMembers.length > 0 && (
                <>
                  <Menu.Separator />
                  <Menu.Heading>On this page</Menu.Heading>
                  {differentPageMembers.map((member) => (
                    <Button
                      onClick={() => handlePressMember(member)}
                      key={member.personId}
                    >
                      <Avatar
                        username={member.username}
                        personId={member.personId}
                        color={member.color}
                        showTooltip={false}
                        isMuted={true}
                      />
                      <span>{member.username}</span>
                    </Button>
                  ))}
                </>
              )}
            </>
          </PresenceDetailsPopover>
        </OverlayContainer>
      )
      }

      <ProfileSettingsDialog
        person={{ ...currentUser }}
        isOpen={profileSettingsState.isOpen}
        onClose={profileSettingsState.close}
        onUpdatePerson={(person) => {
          handleUpdateProfile(person);
          profileSettingsState.close();
        }}
      />
    </>
  );
};

PresenceDetails.defaultProps = {
  defaultOpen: false,
};
