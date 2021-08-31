import styled from 'styled-components';
import { readableColor } from 'polished';

const AvatarWrap = styled.svg`
`;

const Name = styled.text`
  text-anchor: middle;
`;

interface AvatarProps {
  color?: string;
  name: string;
  size?: string;
}

/**
 * Visual representation of a human user
*/
export const Avatar = ({
  color = "#000",
  name,
  size = "1.5em",
  ...props
}: AvatarProps) => {

  let initials;
  if (name) {
    initials = name.split(' ').map(word => word[0]).join('');
  }

  return (
    <AvatarWrap
      width={size}
      height={size}
      viewBox="0 0 24 24"
      {...props}
    >
      <circle
        cx="12"
        cy="12"
        r="12"
        fill={color}
        vectorEffect="non-scaling-stroke"
      />
      <Name
        x="12"
        y="18"
        fill={readableColor(color) || '#fff'}
        vectorEffect="non-scaling-stroke"
        fontSize="18"
      >
        {initials || name.charAt(0)}
      </Name>
    </AvatarWrap>
  );
};
