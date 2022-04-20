import React from 'react';
import { Portal, useDisclosure, AlertDialog, AlertDialogOverlay, AlertDialogContent, AlertDialogHeader, AlertDialogFooter, AlertDialogBody, Button, ButtonGroup } from "@chakra-ui/react"

export const Confirmation = ({
  isOpen,
  title,
  message,
  onConfirm,
  cancelText,
  confirmText,
  onClose
}) => {
  const cancelRef = React.useRef();

  const { isOpen: isConfirmOpen,
    onClose: onConfirmClose } = useDisclosure({
      defaultIsOpen: isOpen,
      onClose: onClose
    })

  return (<AlertDialog
    isOpen={isConfirmOpen}
    leastDestructiveRef={cancelRef}
    onClose={onConfirmClose}
  >
    <Portal>
      <AlertDialogOverlay>
        <AlertDialogContent>
          {title &&
            <AlertDialogHeader fontSize='lg' fontWeight='bold'>
              {title}
            </AlertDialogHeader>
          }
          {message && <AlertDialogBody>
            {message}
          </AlertDialogBody>}
          <AlertDialogFooter>
            <ButtonGroup>
              <Button
                ref={cancelRef}
                onClick={onClose}>
                {cancelText || 'Cancel'}
              </Button>
              <Button
                colorScheme='red'
                onClick={() => {
                  onConfirm();
                  onClose()
                }}>
                {confirmText || 'Confirm'}
              </Button>
            </ButtonGroup>
          </AlertDialogFooter>
        </AlertDialogContent>
      </AlertDialogOverlay>
    </Portal>
  </AlertDialog>);
}