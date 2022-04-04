import { Modal, ModalOverlay, ModalContent, ModalBody, ModalHeader, ModalFooter, Button, ButtonGroup } from '@chakra-ui/react';

export const Confirmation = ({
  isOpen,
  onClose,
  onConfirm,
  title,
  description
}) => {
  return (
    <Modal
      isOpen={isOpen}
      onClose={onClose}
      closeOnOverlayClick={false}
      closeOnEsc={false}
      size='sm'
    >
      <ModalOverlay />
      <ModalContent>
        <ModalHeader>{title}</ModalHeader>
        {description && <ModalBody>{description}</ModalBody>}
        <ModalFooter>
          <ButtonGroup>
            <Button mr={3} onClick={onConfirm}>
              Confirm
            </Button>
            <Button onClick={onClose}>
              Cancel
            </Button>
          </ButtonGroup>
        </ModalFooter>
      </ModalContent>
    </Modal>
  );
};