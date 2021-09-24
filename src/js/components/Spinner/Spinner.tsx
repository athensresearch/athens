import styled, { keyframes } from "styled-components";
import { classnames } from "@/utils/classnames";
import React from "react";

const spin = keyframes`
  0% {
    transform: rotate(0deg);
  }
  100% {
    transform: rotate(360deg);
  }
`;

const appearAndDrop = keyframes`
  0% {
    transform: translateY(-40%);
    opacity: 0;
  }
  100% {
    transform: translateY(0);
    opacity: 1;
  }
`;

const Wrap = styled.div`
  width: ${(props) => props.size};
  height: ${(props) => props.size};
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
  align-self: center;
  margin: auto;
  text-align: center;
  place-items: center;
  animation: ${appearAndDrop} 0.5s ease;
  place-content: center;

  &.placement-center {
    position: absolute;
    top: calc(50% - ${(props) => props.size} / 2);
    left: calc(50% - ${(props) => props.size} / 2);
  }
`;

const Progress = styled.div`
  width: 3em;
  height: 3em;
  border-radius: 1000em;
  border: 1.5px solid var(--background-minus-1);
  border-top-color: var(--link-color);
  animation: ${spin} 1s linear infinite;
`;

const Message = styled.span`
  animation: ${appearAndDrop} ${(props) => props.messageDelay}s 0.75s
    ease-in-out;
  font-size: 14px;
  animation-fill-mode: both;
`;

interface SpinnerProps {
  message?: string | React.ReactNode;
  placement?: "center" | null;
  size?: string;
  messageDelay?: number;
}

export const Spinner = ({
  message,
  placement,
  size,
  messageDelay,
}: SpinnerProps): JSX.Element => (
  <Wrap
    className={classnames(placement && `placement-${placement}`)}
    size={size}
  >
    <Progress />
    {message && <Message messageDelay={messageDelay}>{message}</Message>}
  </Wrap>
);

Spinner.defaultProps = {
  message: "Loading...",
  placement: "center",
  size: "10rem",
  messageDelay: 2,
};
