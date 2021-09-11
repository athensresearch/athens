import styled from 'styled-components';

const IconWrap = styled.svg`
  height: var(--size, 1.5rem);
  width: var(--size, 1.5rem);

  rect {
    fill: var(--link-color);
  }

  text {
    text-transform: uppercase;
    fill: var(--link-color---contrast);
    font-size: 100%;
    font-weight: bold;
    text-anchor: middle;
  }`;

/**
 * Icon representing a database
 */
export const DatabaseIcon = ({ name }) => {
  return <IconWrap viewBox="0 0 24 24">
    <rect
      fill="var(--link-color)"
      height={24}
      width={24}
      rx={4}
      x={0}
      y={0}
    />
    <text
      x="12"
      y="75%"
    >{name ? name.charAt(0) : 'x'}</text>
  </IconWrap>
};