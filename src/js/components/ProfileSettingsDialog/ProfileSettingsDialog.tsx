import React from 'react';
import { withErrorBoundary } from 'react-error-boundary';

import {
  keyframes,
  Modal,
  ModalOverlay,
  ModalFooter,
  Center,
  Flex,
  Box,
  ButtonGroup,
  ModalHeader,
  ModalCloseButton,
  ModalContent,
  ModalBody,
  Text,
  FormControl,
  FormHelperText,
  Input,
  Avatar,
  Button
} from '@chakra-ui/react';

import { HexColorPicker } from "react-colorful";
import { AriaDialogProps } from '@react-types/dialog';
import { OverlayProps } from '@react-aria/overlays';

const pulse = keyframes`
  from {
    transform: translate(-50%, -50%) scale(1) ;
  } to {
    transform: translate(-50%, -50%) scale(1.08);
  }
`;

const ColorPickerWrap = ({ children }) => {
  return (<Box
    sx={{
      ".react-colorful": {
        width: "8.5rem",
        height: "3rem",
        gap: "1rem",
        margin: "-0.25rem 0 1rem",
        flexDirection: "row",

        "> *": {
          borderRadius: "0.5rem",
          height: "100%",
          flex: "0 0 4rem",
        }
      },
      ".react-colorful__saturation": {
        borderBottom: 0
      },
      ".react-colorful__interactive:focus .react-colorful__pointer": {
        animation: `${pulse} 0.5s infinite alternate ease-in-out`
      }
    }}
  >
    {children}
  </Box>)
};

const Inputs = ({ children }) => {
  return (<Flex gap="2rem" align="flex-start" justifyContent="center">
    {children}
  </Flex>)
}

interface ProfileSettingsDialogProps extends OverlayProps, AriaDialogProps {
  person: Person;
  onUpdatePerson: (person: Person) => void;
}
/**
 * Dialog for modifying the current user's username and color
 */
export const _ProfileSettingsDialog = ({
  person,
  onClose: handleClose,
  onUpdatePerson: handleUpdatePerson,
  isOpen,
  ...rest
}: ProfileSettingsDialogProps) => {
  const [ editingUsername, setEditingUsername ] = React.useState<string>(person.username || '');
  const [ editingColor, setEditingColor ] = React.useState<string>(person.color || '#0071DB');
  const [ isValidUsername, setIsValidUsername ] = React.useState<boolean>(!!editingUsername);

  const handleChangeUsername = (e: React.ChangeEvent<HTMLInputElement>) => {
    const attempt = e.target.value.trim();
    setEditingUsername(attempt);
    setIsValidUsername(attempt.length > 1);
  }

  return (
    <Modal
      isOpen={isOpen}
      onClose={handleClose}
      {...rest}
    >
      <ModalOverlay />
      <ModalContent>
        <ModalHeader>
          Change how you appear to others
        </ModalHeader>
        <ModalCloseButton />
        <ModalBody>
          <Center
            padding="2rem"
            display="flex"
            flexDirection="column"
          >
            <Avatar
              name={isValidUsername ? editingUsername : person.username}
              bg={editingColor}
            />
            <Text>{isValidUsername ? editingUsername : person.username}</Text>
          </Center>
          <Inputs>
            <ColorPickerWrap>
              <HexColorPicker color={editingColor} onChange={setEditingColor} />
            </ColorPickerWrap>
            <FormControl>
              <Input
                isRequired
                className={!isValidUsername ? 'is-invalid' : ''}
                type="name"
                defaultValue={editingUsername}
                onChange={handleChangeUsername}
              />
              <FormHelperText
                style={{ opacity: isValidUsername ? 0 : 1 }}
              >At least 2 characters
              </FormHelperText>
            </FormControl>
          </Inputs>
        </ModalBody>
        <ModalFooter
          borderTop="1px solid"
          borderColor="separator.divider"
        >
          <ButtonGroup>
            <Button onClick={handleClose}>Cancel</Button>
            <Button isDisabled={!isValidUsername} onClick={() => handleUpdatePerson({ ...person, username: editingUsername, color: editingColor })}>Change appearance</Button>
          </ButtonGroup>
        </ModalFooter>
      </ModalContent>
    </Modal>
  )
}

export const ProfileSettingsDialog = withErrorBoundary(_ProfileSettingsDialog, { fallback: null });
