import React from 'react';
import styled from 'styled-components';
import { classnames } from '../../utils/classnames';

export interface ButtonProps extends React.ButtonHTMLAttributes<HTMLButtonElement> {
  /**
   * Whether this button should have a stronger style
   */
  isPrimary?: boolean;
  /**
   * Whether the button should appear pressed
   */
  isPressed?: boolean;
  /**
   * Button shape
   */
  shape: 'rect' | 'round',
  /**
   * Button style type
   */
  variant: 'plain' | 'gray' | 'tinted' | 'filled';
}


/**
 * Primary UI component for user interaction
 */
export const Button = styled.button.attrs(props => {
  const shape = props.shape || 'rect';
  const variant = props.variant || 'plain';

  return ({
    className: classnames(
      props.className,
      props.isPrimary
        ? 'variant-tinted'
        : variant && 'variant-' + variant,
      'shape-' + shape
    ),
  ariaPressed: props.isPressed,
  })
}) <ButtonProps>`
  margin: 0;
  font-family: inherit;
  font-size: inherit;
  font-weight: 500;
  border: none;
  display: inline-flex;
  place-items: center;
  place-content: center;
  color: var(--body-text-color);
  background-color: transparent;
  transition-property: filter, background, color, opacity;
  transition-duration: 0.075s;
  transition-timing-function: ease;
  gap: 0.5rem;
  text-align: left;

  &:enabled {
    cursor: pointer;
  }

  span {
    flex: 1 0 auto;
  }

  > svg {
    margin: -0.0835em -0.325rem;

    &:not(:first-child) {
      margin-left: 0.251em;
    }
    &:not(:last-child) {
      margin-right: 0.251em;
    }
  }

  /* Shapes */
  &.shape-rect {
    border-radius: 0.25rem;
    padding: 0.375rem 0.625rem;
  }

  &.shape-round {
    border-radius: 2rem;
    padding: 0.375em 0.8rem;
  }

  /* Variants */
  &.variant-plain { }

  &.variant-gray {
    background: var(--body-text-color---opacity-lower);
  }

  &.variant-tinted {
    background: var(--link-color---opacity-lower);
    color: var(--link-color);
  }

  &.variant-filled {
    background: var(--link-color);
    color: var(--white, #fff);
  }

  /* States */
  &:enabled {
    cursor: pointer;

    &:active,
    &[aria-pressed="true"] {
      transition: filter 0s ease-in-out;

      &.variant-filled {
        filter: brightness(90%);
      }
      &.variant-gray,
      &.variant-plain,
      &.variant-tinted {
        backdrop-filter: brightness(90%);
      }
    }
  }
`;
