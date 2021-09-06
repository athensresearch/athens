import React from 'react';
import styled from 'styled-components';
import { Check, Warning } from '@material-ui/icons';

export interface InputProps extends React.InputHTMLAttributes<HTMLInputElement> { }

export const Input = styled.input<InputProps>`
    padding: 0.375rem 0.625rem;
    margin: 0;
    font-family: inherit;
    font-size: inherit;
    border-radius: 0.25rem;
    font-weight: 500;
    border: none;
    display: inline-flex;
    align-items: center;
    color: var(--body-text-color);
    caret-color: var(--link-color);
    background: var(--body-text-color---opacity-lower);
    transition-property: filter, background, color, opacity, border-color;
    transition-duration: 0.1s;
    transition-timing-function: ease-in-out;
    gap: 0.5rem;

    &:enabled {
      &:hover {
        background: var(--body-text-color---opacity-low);
      }

      &:active {
        background: var(--body-text-color---opacity-low);
      }
    }

    &:disabled {
      color: var(--body-text-color---opacity-low);
      background: var(--body-text-color---opacity-lower);
      cursor: default;
    }

    &.is-invalid:not(:placeholder-shown):not(:empty) {
      color: var(--warning-color);
      background: var(--warning-color---opacity-lower);

      &:hover {
        background: var(--warning-color---opacity-low);
      }

      &:active {
        background: var(--warning-color---opacity-low);
      }
    }

    &.is-valid:not(:placeholder-shown) {
      color: var(--confirmation-color);
      background: var(--confirmation-color---opacity-lower);

      &:hover {
        background: var(--confirmation-color---opacity-low);
      }

      &:active {
        background: var(--confirmation-color---opacity-low);
      }
    }
`;

Input.Label = styled.span`
  font-weight: bold;
`;

Input.Help = styled.small``;

Input.LabelWrapper = styled.label`
  display: grid;
  grid-template-areas: "label" "input" "help";

  .label,
  ${Input.Label} {
    grid-area: label;
  }

  .input,
  ${Input} {
    grid-area: input;
  }

  .help,
  ${Input.Help} {
    grid-area: help;
  }

  .input-right {
    grid-area: input;
    margin: auto 0;
    margin-left: auto;
    z-index: 1;
  }

  .icon-right,
  .icon-left {
    grid-area: input;
    pointer-events: none;
    margin: auto;
  }

  .icon-right {
    margin-right: 0.25rem;

    ~ ${Input} {
      padding-right: 2rem;
    }
  }

  .icon-left {
    margin-left: 0.25rem;

    ~ ${Input} {
      padding-left: 2rem;
    }
  }
`;

Input.Invalid = styled(Warning).attrs({
  className: 'icon-right',
})`
`;

Input.Valid = styled(Check).attrs({
  className: 'icon-right',
})``;
