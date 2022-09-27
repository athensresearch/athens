import { chakra, useStyles, StylesProvider, useMultiStyleConfig, forwardRef } from '@chakra-ui/react';
import { InboxIcon } from '@/Icons/Icons';

export const EmptyIcon = forwardRef(({ Icon, ...props }, ref) => {
  const { children, ...rest } = props;
  const styles = useStyles();
  return <Icon __css={styles.icon} ref={ref} {...rest} >{children}</Icon>
});

EmptyIcon.defaultProps = {
  Icon: InboxIcon,
}

export const EmptyTitle = forwardRef((props, ref) => {
  const { children, ...rest } = props;
  const styles = useStyles();
  return <chakra.h2 __css={styles.title} size="sm" ref={ref} {...rest}>{children}</chakra.h2>
});

export const EmptyMessage = forwardRef((props, ref) => {
  const { children, ...rest } = props;
  const styles = useStyles()
  return <chakra.p __css={styles.message} ref={ref} {...rest}>{children}</chakra.p>
});

export const Empty = forwardRef((props, ref) => {
  const { size, variant, children, ...rest } = props
  const styles = useMultiStyleConfig('Empty', { size, variant })

  return (
    <chakra.div __css={styles.container} ref={ref} {...rest}>
      <StylesProvider value={styles}>{children}</StylesProvider>
    </chakra.div>)
});