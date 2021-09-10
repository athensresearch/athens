import styled from 'styled-components';

const ShortcutWrap = styled.li`
  display: contents;

  a {
      color: var(--link-color);
      text-decoration: none;
      cursor: pointer;
      display: flex;
      flex: 0 0 auto;
      padding: 0.25rem 0;
    }
`;

export interface ShortcutProps {
  /**
   * The index of the shortcut in the list of shortcuts
  */
  order: number;
  /**
   * The title of the shortcut
  */
  title: string;
  /**
   * The UID of the shortcut
  */
  uid: string;
  handlePressShortcut(): void;
}

export const Shortcut = ({ order, title, uid, handlePressShortcut }: ShortcutProps) => {
  return (<ShortcutWrap onClick={handlePressShortcut}>
    <a href="">{title}</a>
  </ShortcutWrap>)
}