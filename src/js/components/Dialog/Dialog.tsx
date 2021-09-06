import styled from 'styled-components';
import { DOMRoot } from '../../config';

import { Modal } from '@material-ui/core'
import { Close } from '@material-ui/icons'

import { Button } from '../Button';
import { Overlay } from '../Overlay';

const DialogWrap = styled(Overlay)`
  width: min(30em, calc(100% - 2rem));
  position: absolute;
  left: 50%;
  top: 50%;
  transform: translate(-50%, -50%);

  --edge-spacing: 1rem;
`;

interface DialogProps extends React.HTMLAttributes<HTMLDivElement> {
  isDialogOpen?: boolean;
  handleClose?: () => void;
  handlePressOk?: () => void;
  handlePressCancel?: () => void;
}

export const Dialog = ({
  children,
  isDialogOpen = true,
  handleClose,
}: DialogProps) => {
  return (
    <Modal
      container={DOMRoot}
      open={isDialogOpen}
      onClose={handleClose}
    >
      <DialogWrap>
        {children}
      </DialogWrap>
    </Modal>);
};

Dialog.Header = styled.header`
  display: flex;
  align-items: center;
  padding-left: var(--edge-spacing);
  padding-right: var(--edge-spacing);
`;

Dialog.CloseButton = styled(Button).attrs({
  children: <Close />
})`
  margin-left: auto;
  margin-right: calc(var(--edge-spacing) * -1);
`;

Dialog.Body = styled.main`
  padding-left: var(--edge-spacing);
  padding-right: var(--edge-spacing);
`;

Dialog.Title = styled.h1`
  margin: 0;
  font-size: var(--font-size--text-lg);
`;

Dialog.Actions = styled.div`
  display: flex;
  justify-content: flex-end;
  padding-left: var(--edge-spacing);
  padding-right: var(--edge-spacing);
`;

