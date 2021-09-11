import { CSSProperties } from 'react';
import styled from 'styled-components';
import { readableColor } from 'polished';

const IconWrap = styled.svg`
  font-size: var(--size, 1.5em);
  height: 1em;
  width: 1em;

  text {
    text-transform: uppercase;
    font-weight: bold;
    user-select: none;
    font-family: var(--font-family-serif);
  }
`;

export interface DatabaseIcon {
    icon?: string;
    name: string;
    color?: CSSProperties["color"];
    size?: CSSProperties["width"];
}

/**
 * Icon representing a database
 */
export const DatabaseIcon = ({
    icon,
    name,
    color,
    size
}: DatabaseIcon): React.ReactElement => {
    // Validate is a valid emoji icon
    const isEmojiIcon = icon && /\p{Emoji}/u.test(icon);

    return (
        <IconWrap
            viewBox="0 0 24 24"
            style={{ "--size": size ? size : undefined }}
        >
            <rect
                fill={color ? color : "var(--link-color)"}
                height={24}
                width={24}
                rx={4}
                x={0}
                y={0}
            />
            <text
                x={12}
                y={13.5}
                fontSize={isEmojiIcon ? 16 : 20}
                textAnchor="middle"
                dominantBaseline="middle"
                fill={color ? readableColor(color) : "var(--link-color---contrast)"}
            >{isEmojiIcon ? icon.trim() : name.trim().charAt(0)}</text>
        </IconWrap>)
};

DatabaseIcon.Wrap = IconWrap;