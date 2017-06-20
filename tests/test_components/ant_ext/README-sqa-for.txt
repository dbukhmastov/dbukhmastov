<sqa-for list="aaa,sss ,ddd" param="suite">
  <sequential>
    <echo message="suite=@{suite}"/>
  </sequential>
</sqa-for>
The result is:
[echo] suite=aaa
[echo] suite=sss
[echo] suite=ddd


<sqa-for begin="1" end="6" step="2" param="ivan">
  <sequential>
    <echo message="suite=@{ivan}"/>
  </sequential>
</sqa-for>
The result is:
[echo] suite=1
[echo] suite=3
[echo] suite=5


<sqa-for begin="7" end="1" step="-3" param="ttt">
  <sequential>
    <echo message="suite=@{ttt}"/>
  </sequential>
</sqa-for>
The result is:
[echo] suite=7
[echo] suite=4
