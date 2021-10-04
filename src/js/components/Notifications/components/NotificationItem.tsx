import React from "react";
import styled, { keyframes } from "styled-components";
import { mergeProps } from "@react-aria/utils";

import { notify, Notification } from '../Notifications';

import { Button } from "@/Button";
import { Icon } from "@/Icons/Icon";
import { X } from "@/Icons/X";

const appear = keyframes`
  from {
    opacity: 0;
    transform: scale(0.95);
  }
  to {
    opacity: 1;
    transform: scale(1);
  }
`;

const Wrap = styled.div`
  box-shadow: 0 0 0 1px var(--shadow-color---opacity-10);
  background: var(--background-plus-2---opacity-med);
  color: var(---body-text-color---opacity-80);
  padding: 0.5rem 1rem;
  border-radius: 1rem;
  opacity: 0;
  transition: all 0.25s ease-out;
  animation: ${appear} 0.25s ease-out;
  backdrop-filter: blur(20px);
  z-index: 1;
  display: flex;
  align-items: center;
  gap: 0.5rem;

  button:not(& * button):last-child {
    margin-right: -0.5rem;
    margin-left: auto;
  }

  svg:not(& * svg):first-child {
    margin-left: -0.5rem;
  }

  &:before {
    content: "";
    position: absolute;
    background: var(--background-color—-opacity-med);
    opacity: 0.5;
    z-index: -1;
    inset: 0;
    border-radius: inherit;
  }

  &.visible {
    opacity: 1;
  }

  &.toast-loading {
    padding: 0.75rem 1.5rem;
  }

  &.toast-success {
    background: var(--confirmation-color---opacity-10);
    color: var(--confirmation-color);
  }

  &.toast-warning {
    background: var(--warning-color---opacity-15);
    color: var(--warning-color);
  }

  &.toast-error {
    background: var(--error-color---opacity-15);
    color: var(--error-color);
  }
`;

const DismissButton = styled(Button)``;

const UndoButton = styled(Button)``;

export const NotificationItem = (t: Notification) => {
  const { isDismissable, onUndo, undoMessage, ...rest } = t;
  const ref = React.useRef<HTMLDivElement>(null);
  const [size, setSize] = React.useState({ width: undefined, height: undefined });

  const setMinSize = () => {
    if (ref.current) {
      setSize({ width: ref.current.offsetWidth, height: ref.current.offsetHeight });
    }
  };

  const handleUndo = () => {
    setMinSize();
    const message = t.undoMessage || "Undone";
    const resultProps = { id: rest.id, onUndo: undefined, isDismissable: true };
    const updateNotification = t.type !== 'blank'
      ? notify[t.type || 'blank'](message, resultProps)
      : notify(message, resultProps);

    onUndo();
    updateNotification()
  }

  return (
    <Wrap
      {...mergeProps(
        rest,
        { className: `toast-${t.type} ${t.visible ? "visible" : ""}` },
        {
          style: {
            opacity: t.visible ? 1 : 0,
            minWidth: size.width,
            minHeight: size.height,
          }
        }
      )}
      ref={ref}
    >
      {t.children}
      {t.onUndo && (
        <UndoButton
          shape="round"
          onClick={handleUndo}
        >
          Undo
        </UndoButton>
      )}
      {t.isDismissable && (
        <DismissButton shape="round" onClick={() => notify.dismiss(t.id)}>
          <Icon style={{ padding: "0.125rem", "--size": "1.25rem" }}>
            <X />
          </Icon>
        </DismissButton>
      )}
    </Wrap>
  );
};