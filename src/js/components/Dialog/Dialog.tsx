import React from 'react';
import styled from 'styled-components';

import {
  useOverlay,
  usePreventScroll,
  useModal,
  OverlayProps,
  OverlayContainer
} from '@react-aria/overlays';
import { useDialog } from '@react-aria/dialog';
import { AriaDialogProps } from '@react-types/dialog';
import { FocusScope } from '@react-aria/focus';
import { mergeProps } from '@react-aria/utils';

import { Button } from '@/Button';
import { Overlay } from '@/Overlay';

const Container = styled(Overlay)`
  display: flex;
  flex-direction: row;
  gap: 1rem;
  width: max-content;
  padding: 1rem;
  position: absolute;
  left: 50%;
  top: 50%;
  transform: translate(-50%, -50%);
  min-width: 20rem;
`;

const Image = styled.div``;

const Title = styled.h1`
  font-size: 1em;
  margin: 0;
`;

const Message = styled.p`
  margin: 0;
`;

const Body = styled.div`
  display: flex;
  flex-direction: column;
  gap: 0.125rem;
  min-width: 15rem;
  flex: 1 1 100%;
`;

const Actions = styled.div`
  display: grid;
  grid-auto-flow: column;
  grid-auto-columns: 1fr;
  gap: 0.25rem;
  width: max-content;
  margin-top: auto;
  margin-left: auto;
  padding-top: 1rem;
  align-self: flex-end;
`;

const Backdrop = styled.div`
  position: fixed;
  z-index: var(--zindex-modal);
  top: 0;
  left: 0;
  bottom: 0;
  right: 0;
  background-color: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
`;

const DismissButton = styled(Button)`
  font-weight: normal;
`;
const ConfirmButton = styled(Button)`
  font-weight: normal;
`;

interface DialogProps extends OverlayProps, AriaDialogProps {
  isOpen: boolean,
  title: string,
  children?: React.ReactNode,
  image?: JSX.Element;
  defaultAction?: 'confirm' | 'dismiss';
  dismiss?: {
    label?: string;
    variant?: 'filled' | 'tinted' | 'gray' | 'plain';
  },
  confirm?: {
    label?: string;
    variant?: 'filled' | 'tinted' | 'gray' | 'plain';
  }
  // onClose?: () => void;
  onConfirm?: () => void;
}

export const Dialog = (props: DialogProps): JSX.Element | null => {
  const {
    isOpen,
    title,
    children,
    image,
    onConfirm: handleConfirm,
    onClose: handleClose,
    defaultAction,
    dismiss,
    confirm
  } = props;

  let ref = React.useRef();
  let { overlayProps, underlayProps } = useOverlay(props, ref);
  let { modalProps } = useModal();
  let { dialogProps, titleProps } = useDialog(props, ref);
  usePreventScroll();

  const dismissProps = {
    ...mergeProps({
      onClick: handleClose,
      label: 'Cancel',
      variant: 'plain',
      autoFocus: defaultAction === 'dismiss',
      ...dismiss,
    })
  }

  const confirmProps = {
    ...mergeProps({
      onClick: handleConfirm,
      label: 'Cancel',
      variant: 'filled',
      autoFocus: defaultAction === 'confirm',
      ...confirm,
    })
  }

  return (
    isOpen ?
      <OverlayContainer>
        <Backdrop {...underlayProps}>
          <FocusScope
            contain
            restoreFocus
            autoFocus
          >
            <Container
              {...overlayProps}
              {...dialogProps}
              {...modalProps}
              ref={ref}
            >
              {children
                ? children
                : (
                  <>
                    {image && <Image>{image}</Image>}
                    (<Body>
                      <Title {...titleProps}>{title}</Title>
                      {children}
                      <Actions>
                        <DismissButton {...dismissProps}>Cancel</DismissButton>
                        <ConfirmButton {...confirmProps}>Confirm</ConfirmButton>
                      </Actions>
                    </Body>)
                  </>
                )}
            </Container>
          </FocusScope>
        </Backdrop>
      </OverlayContainer>
      : null
  );
};

Dialog.defaultProps = {
  defaultAction: 'confirm',
}

Dialog.Container = Container;
Dialog.Image = Image;
Dialog.Title = Title;
Dialog.Message = Message;
Dialog.Body = Body;
Dialog.Actions = Actions;
Dialog.DismissButton = DismissButton;
Dialog.ConfirmButton = ConfirmButton;
