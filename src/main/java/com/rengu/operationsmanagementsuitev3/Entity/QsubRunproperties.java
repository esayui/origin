package com.rengu.operationsmanagementsuitev3.Entity;

import lombok.Data;

import java.io.Serializable;


@Data
public class QsubRunproperties implements Serializable {
//       语法: qsub [-a date_time] [-A account_string] [-e path] [-h] [-I] [-j join] [-k keep] [-l resource_list] [-m mail_options] [-n Node_allocation_Method [-L v1,[v2,[v3,[v4]]]]] [-M user_list] [-N name] [-o path] [-p priority] [-q pool] [-r y|n] [-u user_list] [-v variable_list] [-V] [script]

    /**
     * -a 间。格式为[[[[CC]YY]MM]DD]hhmm[.SS]。CC表示世纪，YY表示年（后两位数字）
     * MM表示月（两位数字）
     * DD表示天（两位数 字）
     * hh表示小时（两位数字）
     * mm表示分（两位数字）
     * SS表示秒（两位数字）
     * 如果DD指定的是未来日子，而未指定MM，则MM缺省值为当前月，否则，MM的缺省值为下个月。
     * 如果hhmm指定的未来时间，而未指定DD,则DD的缺省值为当天，否则，DD的缺省值为明天。
     * 如果提交作业时使用该选项，当 指定时间还没到时，作业状态显示为”W”。
     */
    private String date_time;


    private String account;
    /**
     * -e 指定错误输出文件名，格式为[hostname:]path_home。Hostname是返回错误输出文件的主机名
     * path_home是错误输出文件 的绝对路径，如果指定了相对路径，则相对用户的主目录。
     * 不使用该选项时，缺省值是在用户主目录下，以“作业名.e作业ID”命名的文件
     */
    private String e_path;
    /**
     * -h 指定在提交作业时，设置用户级’u’挂起。如果不指定，则设置挂起类型为’n’,即不挂起。
     */
    private String h;
    /**
     * -I 指定作业以交互方式运行。
     */
    private String I;
    /**
     *  -j 指定合并错误输出和实际输出。如果指定’oe’，则合并到标准输出文件中；如果指定’eo’，则合并到标准错误输出文件中.
     */
    private String join;
    /**
     *  -k 指定执行主机是否保留错误输出和实际输出。
     *  如果指定‘o’,则仅保留标准输出；如果指定’e’，则仅保留标准错误输出；
     *  如果指定’oe’或‘eo’,则保留标准输出和标准错误输出；如果指定’n’,则不保留任何输出。
     */
    private String keep;


    /**
     * -l 指定作业所需要的资源，设定对可消耗资源的限制。
     * 如果不设置，则无限制。例如： resource_name[=[value]][,resource_name[=[value]],…]
     * 例：-l nodes=12  为申请12个节点
     */
    private String resource_list;
    /**
     * -m 定义何时给用户发送有关作业的邮件。可设定的选项有：
     * n 不发送邮件
     * a 当作业被批处理系统中断时 发送邮件
     * b 当作业开始执行时 发送邮件
     * e 当作业执行结束时 发送文件
     */
    private String mail_options;
    private String Node_allocation_Method;



    /**
     * -M 指定发送有关作业信息的邮件用户列表。格式为user[@host][,user@[host],…]缺省值为提交作业的用户。
     */
    private String M_user_list;
    /**
     * -N 指定作业的名字。缺省值为脚本的名字，如果没有指定脚本，则为STDIN。
     */
    private String name;

    /**
     * -o 指定输出文件名，格式为[hostname:]path_home。缺省值是在用户主目录下，以“作业名.e作业ID”命名的文件
     */
    private String o_path;
    /**
     * -p 指定作业的优先级，优先级的范围是[-1024, +1023]。缺省值是没有优先级。
     */
    private String priority;
    /**
     * -q 指定作业的目的地(结点池)，目的地可有三种格式：
     */
    private String pool;
    /**
     * -r y|n 指定作业是否可重新运行。指定‘y’时，作业可以重新运行；指定’n’时，作业不能重新运行。缺省值为’n’。
     */
    private String nNorY;
    private String u_user_list;
    /**
     * -v 格式为variable1,variable2,…或variable1=value,variable2=value,…这些变量和其值可以传递到作业中。
     */
    private String variable_list;
    /**
     * -V 指定qsub命令的所有的环境变量都传递到批处理作业中。
     */
    private String V;
    private String script;

}
