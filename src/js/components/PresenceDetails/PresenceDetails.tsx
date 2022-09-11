import React from "react";

import { withErrorBoundary } from 'react-error-boundary';
import { VStack, Text, HStack, Tooltip, Avatar, AvatarGroup, Menu, MenuDivider, MenuButton, MenuList, MenuGroup, MenuItem, Button, Portal } from '@chakra-ui/react';
import { ProfileSettingsDialog } from "@/ProfileSettingsDialog";

type PersonWithPresence = Person & {
  pageTitle?: string,
  pageUid?: string
  blockUid?: string
}

export interface PresenceDetailsProps {
  hostAddress: HostAddress;
  currentUser?: PersonWithPresence;
  currentPageMembers: PersonWithPresence[];
  differentPageMembers: PersonWithPresence[];
  handleUpdateProfile(person: Person): void;
  handleCopyHostAddress(hostAddress: HostAddress): void;
  handleCopyPermalink?(): void;
  handlePressMember(person: PersonWithPresence): void;
  connectionStatus: ConnectionStatus;
  defaultOpen?: boolean;
}
interface ConnectionButtonProps {
  connectionStatus: ConnectionStatus;
  showablePersons: PersonWithPresence[];
  currentUser: PersonWithPresence;
}

const ConnectionButton = React.forwardRef((props: ConnectionButtonProps, ref) => {
  const { showablePersons, currentUser } = props;
  return (
    <Tooltip label={`${showablePersons.length} member${showablePersons.length === 1 ? '' : 's'} online`}>
      <Button
        ref={ref as any}
        as={MenuButton}
        size="sm"
        bg="transparent"
        borderRadius="full"
        px={1}
        _hover={{
          bg: "background.upper",
        }}
      >
        {currentUser && (
          <HStack>
            <Avatar
              name={currentUser.username}
              bg={currentUser.color}
              size="xs"
            />
            <AvatarGroup size="xs" max={5}>
              {showablePersons.map((member) => (
                <Avatar
                  key={member.personId}
                  name={member.username}
                  bg={member.color} />
              ))}
            </AvatarGroup>
          </HStack>
        )}
      </Button>
    </Tooltip>
  )
});


export const PresenceDetails = withErrorBoundary((props: PresenceDetailsProps) => {
  const {
    hostAddress,
    currentUser,
    currentPageMembers,
    differentPageMembers,
    handleCopyHostAddress,
    handleCopyPermalink,
    handlePressMember,
    handleUpdateProfile,
    connectionStatus,
  } = props;
  const otherPersons = [...currentPageMembers, ...differentPageMembers];
  const showablePersons = otherPersons.length > 0 ? otherPersons : [currentUser];
  const [shouldShowProfileSettings, setShouldShowProfileSettings] = React.useState(false);

  return connectionStatus === "local" ? (
    <></>
  ) : (
    <>
      <Menu placement="bottom-end" size="sm">
        <ConnectionButton
          connectionStatus={connectionStatus}
          showablePersons={showablePersons}
          currentUser={currentUser}
        />
        <Portal>
          <MenuList>
            <>
              {hostAddress && (
                <>
                  <MenuItem onClick={() => handleCopyHostAddress(hostAddress)}>
                    Copy link to workspace
                  </MenuItem>
                  {handleCopyPermalink && <MenuItem onClick={() => handleCopyPermalink()}>
                    Copy link to page
                  </MenuItem>}
                </>
              )}

              {currentUser && (
                <>
                  <MenuDivider />
                  <MenuItem onClick={() => setShouldShowProfileSettings(true)} icon={<Avatar
                    size="xs"
                    marginBlock={-1}
                    name={currentUser.username}
                    bg={currentUser.color}
                  />}>Edit appearance</MenuItem>
                </>
              )}

              {currentPageMembers.length > 0 && (
                <>
                  <MenuDivider />
                  <MenuGroup title="On this page">
                    {currentPageMembers.map((member) => {
                      return (
                        <MenuItem
                          onClick={() => handlePressMember(member)}
                          key={member.personId}
                          icon={<Avatar
                            size="xs"
                            marginBlock={-1}
                            name={member.username}
                            bg={member.color}
                          />}
                        >
                          <Text maxWidth="10em">{member.username}</Text>
                        </MenuItem>
                      )
                    })}
                  </MenuGroup>
                </>
              )}

              {differentPageMembers.length > 0 && (
                <>
                  <MenuDivider />
                  <MenuGroup>
                    {differentPageMembers.map((member) => (
                      <MenuItem
                        onClick={() => handlePressMember(member)}
                        isDisabled={!member.pageTitle}
                        key={member.personId}
                        icon={<Avatar
                          marginBlock={-1}
                          size="xs"
                          name={member.username}
                          bg={member.color}
                        />}
                      >
                        <VStack align="stretch" spacing={0}>
                          <Text maxWidth="10em">{member.username}</Text>
                          <Text maxWidth="10em" color="foreground.secondary">{member.pageTitle}</Text>
                        </VStack>
                      </MenuItem>
                    ))}
                  </MenuGroup>
                </>
              )}
            </>
          </MenuList>
        </Portal>
      </Menu>

      <ProfileSettingsDialog
        person={{ ...currentUser }}
        isOpen={shouldShowProfileSettings}
        onClose={() => setShouldShowProfileSettings(false)}
        onUpdatePerson={(person) => {
          handleUpdateProfile(person);
          setShouldShowProfileSettings(false)
        }}
      />
    </>
  );
}, { fallback: <></> });
