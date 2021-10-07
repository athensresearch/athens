import styled from 'styled-components';
import { spin } from '../Spinner';

const Svg = styled.svg`
  width: var(--size, 2em);
  height: var(--size, 2em);
  animation: ${spin} 3.33s linear infinite;
`;

export const Indeterminate = (props) => <Svg
  viewBox="0 0 24 24"
  fill="none"
  {...props}
>
  <circle
    cx="12"
    cy="12"
    r="9.25"
    stroke="currentColor"
    strokeWidth="5"
    strokeMiterlimit="16"
    strokeDasharray="1 2.2222"
  />
</Svg>
