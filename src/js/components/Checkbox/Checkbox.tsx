import React from 'react';
import styled, { keyframes } from 'styled-components';
import { classnames } from '../../utils/classnames';

import { useCheckbox } from '@react-aria/checkbox'
import { useToggleState } from '@react-stately/toggle';

const HiddenInput = styled.input`
  visibility: hidden;
  position: absolute;
`;

const CheckboxWrap = styled.svg`
  height: var(--size, 1em);
  width: var(--size, 1em);
  vertical-align: calc(var(--size, 1em) * -0.125);
  transition: transform 0.2s ease-in-out;
`;

const Label = styled.label`
  &:hover {
    color: var(--body-text-color---opacity-high);
  }

  &:active {
    user-select: none;
    color: var(--body-text-color---opacity-med);

    ${CheckboxWrap} {
      transition: transform 0.05s ease-in-out;
      transform: scale(1.1);
    }
  }

  &.is-disabled {
    color: var(--body-text-color---opacity-high);
  }

  &.has-children {
    ${CheckboxWrap} {
      margin-inline-end: 0.25em;
    }

    &.is-checked {
      color: var(--body-text-color---opacity-med);
      
      &.should-strikethrough-when-checked {
        text-decoration: line-through;
      }
    }
  }
`;

const MarkAppear = keyframes`
  from {
    opacity: 0;
    transform: scale(0.5);
  }
  to {
    opacity: 1;
    transform: scale(1);
  }
`;

const Field = styled.rect`
  stroke: var(--field-color, var(--link-color));
  stroke-width: var(--stroke-width, 8%);
  x: 4%;
  y: 4%;
  width: calc(100% - 8%);
  height: calc(100% - 8%);
  rx: var(--rx, 12%);
  fill: transparent;
  transition: all 0.2s ease-in-out;

  .is-emphasized & {
    --field-color: var(--confirmation-color);
  }

  .is-indeterminate &,
  .is-checked &{
    x: 0;
    y: 0;
    width: 100%;
    height: 100%;
    rx: var(--rx, 14%);
    stroke-width: 0;
    fill: var(--field-color, var(--link-color));
  }

  .is-disabled & {
    stroke: none;
    fill: var(--body-text-color---opacity-low);
    x: 0;
    y: 0;
    width: 100%;
    height: 100%;
  }

  .style-circle & {
    rx: 100%;

    .is-indeterminate &,
    .is-checked &{
      rx: 100%;
    }
  }

`;

const Mark = styled.path`
  stroke: var(--mark-color, #fff);
  stroke-width: 3;
  stroke-linecap: round;
  fill: none;
  transform-origin: 50% 50%;
  animation: ${MarkAppear} 0.2s ease-in-out;
`;


interface CheckboxProps {
  onChange?: (isSelected: boolean) => void;
  isReadOnly?: boolean;
  defaultSelected?: boolean;
  isSelected?: boolean;
  isIndeterminate?: boolean;
  isEmphasized?: boolean;
  isDisabled?: boolean;
  children?: React.ReactNode;
  styleCircle?: React.ReactNode;
  shouldStrikethroughWhenChecked?: boolean;
}

const Checkbox = (props: CheckboxProps) => {
  let {
    isIndeterminate = false,
    isEmphasized = false,
    isDisabled = false,
    shouldStrikethroughWhenChecked = false,
    children,
  } = props;
  let inputRef = React.useRef<HTMLInputElement>(null);
  let { inputProps } = useCheckbox(props, useToggleState(props), inputRef);

  return (
    <Label
      className={
        classnames(
          inputProps.checked && 'is-checked',
          isDisabled && 'is-disabled',
          isEmphasized && 'is-emphasized',
          isIndeterminate && 'is-indeterminate',
          shouldStrikethroughWhenChecked && 'should-strikethrough-when-checked',
          props.styleCircle && 'style-circle',
          !!children && 'has-children'
        )
      }>
      <HiddenInput {...inputProps} ref={inputRef} />
      <CheckboxWrap
        viewBox="0 0 24 24"
        aria-hidden="true">
        <Field />
        {isIndeterminate ? (
          <Mark as="line" x1="7" y1="12" x2="17" y2="12" />
        ) : (
          inputProps.checked && (
            <Mark d="M6 12.026l4.31 4.474L18.5 8" />
          )
        )}
      </CheckboxWrap>
      {children}
    </Label>
  );
}

let _Checkbox = React.forwardRef(Checkbox);
export { _Checkbox as Checkbox };